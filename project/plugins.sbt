addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.0")
addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.11.4")
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)