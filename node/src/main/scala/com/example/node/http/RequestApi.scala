package com.example.node.http

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.node.NodeEntity._
import com.example.node._
import com.example.node.query.GetAllReferrerNodes
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, Formats, native}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object RequestApi extends Json4sSupport {
  implicit val timeout: Timeout = 3.seconds
  implicit val serialization = native.Serialization
  implicit val formats: Formats = DefaultFormats

  def route(nodeCordinator: ActorRef[ShardingEnvelope[NodeCommand[NodeCommandReply]]])
    (implicit system: akka.actor.typed.ActorSystem[Nothing], ec: ExecutionContext): Route = {

    pathPrefix("api") {
      pathPrefix("node") {
        pathPrefix(Segment) { nodeId =>
          pathPrefix("event") {
            pathPrefix("start") {
              post {
                entity(as[NodeVisitStart]) { event =>
                  complete(
                    nodeCordinator.ask[NodeCommandReply] { ref =>
                      ShardingEnvelope(nodeId, NodeVisitCommand(NodeVisitEvent(System.currentTimeMillis(), event), ref))
                    }
                  )
                }
              }
            } ~
            pathPrefix("end") {
              post {
                entity(as[NodeVisitEnd]) { event =>
                  complete(
                    nodeCordinator.ask[NodeCommandReply] { ref =>
                      ShardingEnvelope(nodeId, NodeVisitCommand(NodeVisitEvent(System.currentTimeMillis(), event), ref))
                    }
                  )
                }
              }
            } ~
            pathPrefix("focus") {
              post {
                entity(as[NodeVisitFocus]) { event =>
                  complete(
                    nodeCordinator.ask[NodeCommandReply] { ref =>
                      ShardingEnvelope(nodeId, NodeVisitCommand(NodeVisitEvent(System.currentTimeMillis(), event), ref))
                    }
                  )
                }
              }
            } ~
            pathPrefix("scroll") {
              post {
                entity(as[NodeVisitScroll]) { event =>
                  complete(
                    nodeCordinator.ask[NodeCommandReply] { ref =>
                      ShardingEnvelope(nodeId, NodeVisitCommand(NodeVisitEvent(System.currentTimeMillis(), event), ref))
                    }
                  )
                }
              }
            }
          } ~
          pathPrefix("query") {
            pathPrefix("referrers") {
              get {
                val query = GetAllReferrerNodes()
                val result = nodeCordinator.ask[NodeCommandReply] { ref =>
                  ShardingEnvelope(nodeId, NodeQueryCommand(NodeQuery(List(query)), ref))
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

