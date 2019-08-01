import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile = dependencies(
    shared = Seq("com.iheart" %% "ficus" % "1.4.3"),
    play25 = Seq("uk.gov.hmrc" %% "http-verbs" % "9.3.0-play-25" % Provided, "uk.gov.hmrc" %% "domain" % "5.6.0-play-25" % Provided),
    play26 = Seq("uk.gov.hmrc" %% "http-verbs" % "9.3.0-play-26" % Provided, "uk.gov.hmrc" %% "domain" % "5.6.0-play-26" % Provided)
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.mockito" % "mockito-core" % "2.10.0" % Test
  )

  def apply() = compile ++ test
}
