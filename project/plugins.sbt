resolvers += Resolver.bintrayIvyRepo("hmrc", "sbt-plugin-releases")
resolvers += Resolver.bintrayRepo("hmrc", "releases")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "2.13.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "1.13.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-play-cross-compilation" % "2.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-service-manager" % "0.8.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.5")
addSbtPlugin(
  sys.env.getOrElse("PLAY_VERSION", "2.7") match {
    case "2.6" => "com.typesafe.play" % "sbt-plugin" % "2.6.25"
    case "2.7" => "com.typesafe.play" % "sbt-plugin" % "2.7.9"
    case "2.8" => "com.typesafe.play" % "sbt-plugin" % "2.8.7"
  }
)