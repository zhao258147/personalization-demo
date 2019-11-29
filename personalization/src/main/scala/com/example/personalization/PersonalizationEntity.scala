package com.example.personalization

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.example.personalization.query.{PageViewQuery, PageViewQueryResult}

object PersonalizationEntity {
  case class PersonalizationQuery(eventTree: List[PageViewQuery])

  sealed trait PersonalizationCommand[Reply <: PersonalizationCommandReply] {
    def replyTo: ActorRef[Reply]
  }
  case class PageViewCommand(event: PersonalizationEvent, replyTo: ActorRef[PersonalizationCommandReply]) extends PersonalizationCommand[PersonalizationCommandReply]
  case class QueryCommand(query: PersonalizationQuery, replyTo: ActorRef[PersonalizationCommandReply]) extends PersonalizationCommand[PersonalizationCommandReply]

  sealed trait PersonalizationCommandReply
  case object PersonalizationCommandSuccess extends PersonalizationCommandReply
  case class PersonalizationQueryResponse(result: List[PageViewQueryResult]) extends PersonalizationCommandReply
  case class PersonalizationCommandFailed(message: String) extends PersonalizationCommandReply

  sealed trait PersonalizationEvent {
    val timestamp: Long
  }
  case class PageViewEvent(timestamp: Long, eventType: PageViewEventType) extends PersonalizationEvent

  case class PersonalizationState(events: List[PersonalizationEvent])

  private def commandHandler(context: ActorContext[PersonalizationCommand[PersonalizationCommandReply]]): (PersonalizationState, PersonalizationCommand[PersonalizationCommandReply]) => ReplyEffect[PersonalizationEvent, PersonalizationState] = { (state, command) =>
    command match {
      case PageViewCommand(event, replyTo) =>
        Effect.persist(event).thenReply(replyTo)(_ => PersonalizationCommandSuccess)
      case QueryCommand(query: PersonalizationQuery, replyTo) =>
        val results = query.eventTree.foldLeft(List.empty[PageViewQueryResult]){
          case (acc, q) =>
            q.getResult(state.events) +: acc
        }
        Effect.reply(replyTo)(PersonalizationQueryResponse(results))
    }
  }

  private def eventHandler(context: ActorContext[PersonalizationCommand[PersonalizationCommandReply]]): (PersonalizationState, PersonalizationEvent) => PersonalizationState = { (state, event) =>
    event match {
      case pv: PageViewEvent =>
        state.copy(events = pv +: state.events)
    }
  }

  val personalizationEntityTypeKey = EntityTypeKey[PersonalizationCommand[PersonalizationCommandReply]]("PersonalizationEntity")

  def personalizationEntityBehaviour(persistenceId: PersistenceId): Behavior[PersonalizationCommand[PersonalizationCommandReply]] = Behaviors.setup { context =>
    EventSourcedBehavior.withEnforcedReplies(
      persistenceId,
      PersonalizationState(List.empty),
      commandHandler(context),
      eventHandler(context)
    ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 20, keepNSnapshots = 2))

  }
}
