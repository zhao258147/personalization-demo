package com.example.graph.http

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.personalization.{PageViewEnd, PageViewFocus, PageViewScroll, PageViewStart}
import com.example.personalization.PersonalizationEntity._
import com.example.personalization.query.GetAllNodes
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, Formats, native}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object RequestApi extends Json4sSupport {
  implicit val timeout: Timeout = 3.seconds
  implicit val serialization = native.Serialization
  implicit val formats: Formats = DefaultFormats

  def route(personalizationCordinator: ActorRef[ShardingEnvelope[PersonalizationCommand[PersonalizationCommandReply]]])
    (implicit system: akka.actor.typed.ActorSystem[Nothing], ec: ExecutionContext): Route = {

    pathPrefix("api") {
      pathPrefix("person") {
        pathPrefix(Segment) { personId =>
          pathPrefix("event") {
            pathPrefix("start") {
              post {
                entity(as[PageViewStart]) { event =>
                  complete(
                    personalizationCordinator.ask[PersonalizationCommandReply] { ref =>
                      ShardingEnvelope(personId, PageViewCommand(PageViewEvent(System.currentTimeMillis(), event), ref))
                    }
                  )
                }
              }
            } ~
              pathPrefix("end") {
                post {
                  entity(as[PageViewEnd]) { event =>
                    complete(
                      personalizationCordinator.ask[PersonalizationCommandReply] { ref =>
                        ShardingEnvelope(personId, PageViewCommand(PageViewEvent(System.currentTimeMillis(), event), ref))
                      }
                    )
                  }
                }
              } ~
              pathPrefix("focus") {
                post {
                  entity(as[PageViewFocus]) { event =>
                    complete(
                      personalizationCordinator.ask[PersonalizationCommandReply] { ref =>
                        ShardingEnvelope(personId, PageViewCommand(PageViewEvent(System.currentTimeMillis(), event), ref))
                      }
                    )
                  }
                }
              } ~
              pathPrefix("scroll") {
                post {
                  entity(as[PageViewScroll]) { event =>
                    complete(
                      personalizationCordinator.ask[PersonalizationCommandReply] { ref =>
                        ShardingEnvelope(personId, PageViewCommand(PageViewEvent(System.currentTimeMillis(), event), ref))
                      }
                    )
                  }
                }
              }
          } ~
          pathPrefix("query") {
            pathPrefix("referrers") {
              get {
                val query = GetAllNodes()
                val result = personalizationCordinator.ask[PersonalizationCommandReply] { ref =>
                  ShardingEnvelope(personId, QueryCommand(PersonalizationQuery(List(query)), ref))
                }
                complete(result)
              }
            }
          }
        }
      }
    }
  }
}
