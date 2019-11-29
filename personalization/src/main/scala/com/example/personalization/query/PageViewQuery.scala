package com.example.personalization.query

import java.util.UUID

import com.example.personalization.{PageViewEventType, PageViewStart}
import com.example.personalization.PersonalizationEntity.{PageViewEvent, PersonalizationEvent}

sealed trait PageViewQuery {
  type R <: PageViewQueryResult

  def getResult(events: List[PersonalizationEvent]): R
}

case class PageViewEventSumQuery(start: Long, end: Long, uuid: UUID, eventType: PageViewEventType, times: Int) extends PageViewQuery {
  type R = SumQueryResult

  def getResult(events: List[PersonalizationEvent]): SumQueryResult = {
    val result = events.foldLeft(0) {
      case (sum, event: PageViewEvent) if event.timestamp < end && event.timestamp > start && event.eventType == eventType =>
        sum + 1
      case (sum, _) =>
        sum
    }
    SumQueryResult(result)
  }
}

case class PageViewEventFreqQuery(start: Long, end: Long, uuid: UUID, eventType: PageViewEventType, eventTypes: List[PageViewEventType], freq: Int) extends PageViewQuery {
  type R = FrequencyQueryResult
  case class IntermediateResult(eventSum: Int, total: Int)
  override def getResult(events: List[PersonalizationEvent]): FrequencyQueryResult = {
    val result = events.foldLeft(IntermediateResult(0, 0)) {
      case (acc: IntermediateResult, event: PageViewEvent) if event.timestamp < end && event.timestamp > start && eventTypes.contains(event.eventType) && event.eventType == eventType =>
        acc.copy(eventSum = acc.eventSum + 1, total = acc.total + 1)
      case (acc: IntermediateResult, event: PageViewEvent) if event.timestamp < end && event.timestamp > start && eventTypes.contains(event.eventType) =>
        acc.copy(total = acc.total + 1)
      case (acc: IntermediateResult, _) =>
        acc
    }
    FrequencyQueryResult(result.eventSum.toDouble/result.total.toDouble)
  }
}

case class GetAllNodes() extends PageViewQuery {
  type R = GetAllNodesResult
  case class IntermediateResult(eventSum: Int, total: Int)
  override def getResult(events: List[PersonalizationEvent]): GetAllNodesResult = {
    val pageViews: Seq[PageViewStart] = events.flatMap{
      case event: PageViewEvent =>
        event.eventType match {
          case eventType: PageViewStart =>
            Some(eventType)
          case _ =>
            None
        }
      case _ =>
        None
    }
    GetAllNodesResult(pageViews)
  }
}

sealed trait PageViewQueryResult
case class SumQueryResult(sum: Int) extends PageViewQueryResult
case class FrequencyQueryResult(frequency: Double) extends PageViewQueryResult
case class GetAllNodesResult(nodes: Seq[PageViewStart]) extends PageViewQueryResult