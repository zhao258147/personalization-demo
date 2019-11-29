package com.example.node

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.example.node.query.{NodeVisitQuery, NodeVisitQueryResult}

object NodeEntity {
  case class NodeQuery(eventTree: List[NodeVisitQuery])

  sealed trait NodeCommand[Reply <: NodeCommandReply] {
    def replyTo: ActorRef[Reply]
  }
  case class NodeVisitCommand(event: NodeEvent, replyTo: ActorRef[NodeCommandReply]) extends NodeCommand[NodeCommandReply]
  case class NodeQueryCommand(query: NodeQuery, replyTo: ActorRef[NodeCommandReply]) extends NodeCommand[NodeCommandReply]

  sealed trait NodeCommandReply
  case object NodeCommandSuccess extends NodeCommandReply
  case class NodeQueryResponse(result: List[NodeVisitQueryResult]) extends NodeCommandReply
  case class NodeCommandFailed(message: String) extends NodeCommandReply

  sealed trait NodeEvent
  case class NodeVisitEvent(timestamp: Long, eventType: NodeVisitEventType) extends NodeEvent

  case class NodeState(events: List[NodeEvent])

  private def commandHandler(context: ActorContext[NodeCommand[NodeCommandReply]]): (NodeState, NodeCommand[NodeCommandReply]) => ReplyEffect[NodeEvent, NodeState] = { (state, command) =>
    command match {
      case nv: NodeVisitCommand =>
        Effect.persist(nv.event).thenReply(nv.replyTo)(_ => NodeCommandSuccess)
      case nq: NodeQueryCommand =>
        val results = nq.query.eventTree.foldLeft(List.empty[NodeVisitQueryResult]){
          case (acc, q) =>
            q.getResult(state.events) +: acc
        }
        Effect.reply(nq.replyTo)(NodeQueryResponse(results))
    }
  }

  private def eventHandler(context: ActorContext[NodeCommand[NodeCommandReply]]): (NodeState, NodeEvent) => NodeState = { (state, event) =>
    event match {
      case nv: NodeVisitEvent =>
        state.copy(events = nv +: state.events)
    }
  }

  def nodeEntityBehaviour(persistenceId: PersistenceId): Behavior[NodeCommand[NodeCommandReply]] = Behaviors.setup { context =>
    EventSourcedBehavior.withEnforcedReplies(
      persistenceId,
      NodeState(List.empty),
      commandHandler(context),
      eventHandler(context)
    ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 20, keepNSnapshots = 2))

  }
}
