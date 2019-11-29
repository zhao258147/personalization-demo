import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerRepository, dockerUpdateLatest}
import sbt.Keys.scalaVersion

val akkaVersion = "2.6.0"
val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
val akkaStreams = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaShardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion
val akkaClusterTyped = "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion
val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion
val akkaSBR = "com.lightbend.akka" %% "akka-split-brain-resolver" % "1.1.10"
val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.99"
val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % "1.0.4"
val akkaManagementClusterHttp = "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.4"
val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.4"
val akkaDiscoveryK8sApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.4"
val akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % akkaVersion
val akkaSerialization = "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion
val ficus = "com.iheart" %% "ficus" % "1.4.3"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.10"
val akkaHttpJson4s = "de.heikoseeberger" %% "akka-http-json4s" % "1.29.1"
val json4sNative = "org.json4s" %% "json4s-native" % "3.5.4"
val json4sExt = "org.json4s" %% "json4s-ext" % "3.5.4"
val slf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val leveldb = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"

val harborRep = "10.16.18.14/ims"
val imageName_user = "user"
val imageName_saga = "saga"
val imageName_ruleengine = "ruleengine"
val imageName_akkastream = "akkastream"
val imageTag = "master"

val commonDependencies = Seq(
  akkaActorTyped,
  akkaPersistenceTyped,
  akkaHttp,
  akkaShardingTyped,
  akkaClusterTyped,
  akkaClusterTools,
  akkaSBR,
  akkaPersistenceCassandra,
  akkaPersistenceQuery,
  akkaManagement,
  akkaManagementClusterHttp,
  akkaManagementClusterBootstrap,
  akkaDiscovery,
  akkaSerialization,
  ficus,
  akkaHttpJson4s,
  json4sNative,
  json4sExt,
  scalaTest,
  logback,
  scalaTest,
  leveldb,
//  Cinnamon.library.cinnamonCHMetrics,
  Cinnamon.library.cinnamonAkka,
  Cinnamon.library.cinnamonAkkaHttp,
  Cinnamon.library.cinnamonJvmMetricsProducer,
  Cinnamon.library.cinnamonPrometheus,
  Cinnamon.library.cinnamonPrometheusHttpServer
)

lazy val `user` = (project in file("user"))
  .settings(Seq(
    name := "user",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_user, Some(imageTag)),
    dockerUpdateLatest := true,
    javaOptions in Universal ++= Seq(
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm",
      "-Dcom.sun.management.jmxremote=true",
      "-Dcom.sun.management.jmxremote.port=8888",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.rmi.port=8888",
      "-Djava.rmi.server.hostname=localhost"
    ),
    dockerExposedPorts ++= Seq(9001, 9999),
    libraryDependencies ++= commonDependencies
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)

lazy val `personalization` = (project in file("personalization"))
  .settings(Seq(
    name := "personalization",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_ruleengine, Some(imageTag)),
    dockerUpdateLatest := true,
    javaOptions in Universal ++= Seq(
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm"
    ),
    libraryDependencies ++= commonDependencies
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)


lazy val `node` = (project in file("node"))
  .settings(Seq(
    name := "node",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_ruleengine, Some(imageTag)),
    dockerUpdateLatest := true,
    javaOptions in Universal ++= Seq(
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm"
    ),
    libraryDependencies ++= commonDependencies
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)


lazy val `ruleengine` = (project in file("ruleengine"))
  .settings(Seq(
    name := "ruleengine",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_ruleengine, Some(imageTag)),
    dockerUpdateLatest := true,
    javaOptions in Universal ++= Seq(
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm"
    ),
    libraryDependencies ++= commonDependencies
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)


lazy val `saga` = (project in file("saga"))
  .settings(Seq(
    name := "saga",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_saga, Some(imageTag)),
    dockerUpdateLatest := true,
    javaOptions in Universal ++= Seq(
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm"
    ),
    libraryDependencies ++= commonDependencies
  )
).dependsOn(`user`, `ruleengine`).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)


lazy val `akkastream` = (project in file("akkastream"))
  .settings(Seq(
    name := "akkastream",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_akkastream, Some(imageTag)),
    dockerUpdateLatest := true,
    libraryDependencies ++= commonDependencies,
    libraryDependencies ++= Seq(
      akkaStreams,
      Cinnamon.library.cinnamonAkkaStream
    )
  )
).dependsOn(`saga`).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)


lazy val `graph` = (project in file("graph"))
  .settings(Seq(
    name := "graph",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    dockerAlias := DockerAlias(Some(harborRep), None, imageName_akkastream, Some(imageTag)),
    dockerUpdateLatest := true,
    libraryDependencies ++= commonDependencies,
    libraryDependencies ++= Seq(
      akkaStreams,
      Cinnamon.library.cinnamonAkkaStream
    )
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)



