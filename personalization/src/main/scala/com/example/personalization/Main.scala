package com.example.personalization

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardingEnvelope}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import com.example.personalization.config.PersonalizationConfig
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import PersonalizationEntity._
import akka.actor.typed.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.persistence.typed.PersistenceId
import akka.stream.ActorMaterializer
import com.example.personalization.http.{CORSHandler, RequestApi}

object Main extends App {
  import akka.actor.typed.scaladsl.adapter._

  val conf = ConfigFactory.load()
  implicit val system = ActorSystem("ClusterSystem", conf)
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val typedSystem: akka.actor.typed.ActorSystem[Nothing] = system.toTyped


  val sharding = ClusterSharding(typedSystem)


  val config = conf.as[PersonalizationConfig]("PersonalizationConfig")

  val settings = ClusterShardingSettings(typedSystem)

  val TypeKey = EntityTypeKey[PersonalizationCommand[PersonalizationCommandReply]]("Personalization")

  val shardRegion: ActorRef[ShardingEnvelope[PersonalizationCommand[PersonalizationCommandReply]]] =
    sharding.init(Entity(TypeKey)(createBehavior = entityContext => personalizationEntityBehaviour(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))))


  val route: Route = RequestApi.route(shardRegion)

  private val cors = new CORSHandler {}

  Http()(system).bindAndHandle(
    cors.corsHandler(route),
    config.http.interface,
    config.http.port
  )
}
