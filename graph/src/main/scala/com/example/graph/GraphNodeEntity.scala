package com.example.graph

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}

object GraphNodeEntity {

  sealed trait GraphNodeCommand[Reply <: GraphNodeCommandReply] {
    def replyTo: ActorRef[Reply]
  }

  sealed trait GraphNodeCommandReply
  case object GraphNodeCommandSuccess extends GraphNodeCommandReply

  sealed trait GraphNodeEvent
  case class GraphNodeCreate() extends GraphNodeEvent
  case class GraphNodeNewEdge() extends GraphNodeEvent
  case class GraphNodeRemoveEdge() extends GraphNodeEvent
  case class GraphNodeUpdateEdge() extends GraphNodeEvent

  case class GraphNodeState(events: List[GraphNodeEvent])

  private def commandHandler(context: ActorContext[GraphNodeCommand[GraphNodeCommandReply]]): (GraphNodeState, GraphNodeCommand[GraphNodeCommandReply]) => ReplyEffect[GraphNodeEvent, GraphNodeState] = { (state, command) =>
    command match {
      case cmd =>
        Effect.reply(cmd.replyTo)(GraphNodeCommandSuccess)

    }
  }

  private def eventHandler(context: ActorContext[GraphNodeCommand[GraphNodeCommandReply]]): (GraphNodeState, GraphNodeEvent) => GraphNodeState = { (state, event) =>
    event match {
      case nv: GraphNodeCreate =>
        state.copy(events = nv +: state.events)
    }
  }

  def nodeEntityBehaviour(persistenceId: PersistenceId): Behavior[GraphNodeCommand[GraphNodeCommandReply]] = Behaviors.setup { context =>
    EventSourcedBehavior.withEnforcedReplies(
      persistenceId,
      GraphNodeState(List.empty),
      commandHandler(context),
      eventHandler(context)
    ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 20, keepNSnapshots = 2))

  }
}
