import PlayCrossCompilation._
import sbt._
import uk.gov.hmrc.playcrosscompilation.PlayVersion._

private object BuildDependencies {

  val compile = DependenciesSeq(
    "net.ceedubs" %% "ficus" % "1.1.2",
    "uk.gov.hmrc" %% "http-core" % "0.6.0" % Provided crossPlay Play25,
    "uk.gov.hmrc" %% "http-core" % "1.2.0" % Provided crossPlay Play26
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.mockito" % "mockito-core" % "2.10.0" % Test
  )

  def apply() = compile ++ test
}
