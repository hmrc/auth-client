import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile = dependencies(
    shared = Seq("net.ceedubs" %% "ficus" % "1.1.2"),
    play25 = Seq("uk.gov.hmrc" %% "http-core" % "0.6.0" % Provided),
    play26 = Seq("uk.gov.hmrc" %% "http-core" % "1.2.0" % Provided)
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.mockito" % "mockito-core" % "2.10.0" % Test
  )

  def apply() = compile ++ test
}
