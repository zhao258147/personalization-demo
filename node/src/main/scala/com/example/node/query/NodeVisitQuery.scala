package com.example.node.query

import java.util.UUID

import com.example.node.NodeEntity.{NodeEvent, NodeVisitEvent}
import com.example.node.{NodeVisitEventType, NodeVisitStart}

sealed trait NodeVisitQuery {
  type R <: NodeVisitQueryResult

  def getResult(events: List[NodeEvent]): R
}

case class NodeVisitSumQuery(start: Long, end: Long, eventType: NodeVisitEventType) extends NodeVisitQuery {
  type R = NodeVisitSumQueryResult

  def getResult(events: List[NodeEvent]): NodeVisitSumQueryResult = {
    val result = events.foldLeft(0) {
      case (sum, event: NodeVisitEvent) if event.timestamp < end && event.timestamp > start && event.eventType == eventType =>
        sum + 1
      case (sum, _) =>
        sum
    }
    NodeVisitSumQueryResult(result)
  }

}

case class NodeVisitFreqQuery(start: Long, end: Long, eventType: NodeVisitEventType, eventTypes: List[NodeVisitEventType]) extends NodeVisitQuery {
  type R = NodeVisitFreqQueryResult
  case class IntermediateResult(eventSum: Int, total: Int)
  override def getResult(events: List[NodeEvent]): NodeVisitFreqQueryResult = {
    val result = events.foldLeft(IntermediateResult(0, 0)) {
      case (acc: IntermediateResult, event: NodeVisitEvent) if event.timestamp < end && event.timestamp > start && eventTypes.contains(event.eventType) && event.eventType == eventType =>
        acc.copy(eventSum = acc.eventSum + 1, total = acc.total + 1)
      case (acc: IntermediateResult, event: NodeVisitEvent) if event.timestamp < end && event.timestamp > start && eventTypes.contains(event.eventType) =>
        acc.copy(total = acc.total + 1)
      case (acc: IntermediateResult, _) =>
        acc
    }
    NodeVisitFreqQueryResult(result.eventSum.toDouble/result.total.toDouble)
  }
}

case class GetAllReferrerNodes() extends NodeVisitQuery {
  override type R = NodeReferrerQueryResult

  override def getResult(events: List[NodeEvent]): NodeReferrerQueryResult = {
    val result: Seq[NodeVisitStart] = events.collect{
      case event: NodeVisitEvent =>
        event.eventType match {
          case start: NodeVisitStart =>
            Some(start)
          case _ =>
            None
        }
    }.flatten

    NodeReferrerQueryResult(result)
  }
}


sealed trait NodeVisitQueryResult

case class NodeVisitSumQueryResult(sum: Int) extends NodeVisitQueryResult
case class NodeVisitFreqQueryResult(freq: Double) extends NodeVisitQueryResult
case class NodeReferrerQueryResult(referrers: Seq[NodeVisitStart]) extends NodeVisitQueryResult