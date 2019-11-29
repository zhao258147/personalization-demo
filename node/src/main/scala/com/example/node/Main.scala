package com.example.node

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardingEnvelope}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.persistence.typed.PersistenceId
import akka.stream.ActorMaterializer
import com.example.node.NodeEntity._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import com.example.node.config.NodeConfig
import com.example.node.http.{CORSHandler, RequestApi}
import com.typesafe.config.ConfigFactory

object Main extends App {
  import akka.actor.typed.scaladsl.adapter._

  val conf = ConfigFactory.load()
  implicit val system = ActorSystem("ClusterSystem", conf)
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val typedSystem: akka.actor.typed.ActorSystem[Nothing] = system.toTyped


  val sharding = ClusterSharding(typedSystem)

  val config = conf.as[NodeConfig]("NodeConfig")

  val settings = ClusterShardingSettings(typedSystem)

  val TypeKey = EntityTypeKey[NodeCommand[NodeCommandReply]]("Node")

  val shardRegion: ActorRef[ShardingEnvelope[NodeCommand[NodeCommandReply]]] =
    sharding.init(Entity(TypeKey)(createBehavior = entityContext => nodeEntityBehaviour(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))))


  val route: Route = RequestApi.route(shardRegion)

  private val cors = new CORSHandler {}

  Http()(system).bindAndHandle(
    cors.corsHandler(route),
    config.http.interface,
    config.http.port
  )
  println(config.http.port)

}
