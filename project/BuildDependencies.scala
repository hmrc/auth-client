import sbt._

private object BuildDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "http-core" % "0.6.0" % "provided",
    "net.ceedubs" %% "ficus" % "1.1.2"
  )

  val testScope = "test"
  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % testScope,
    "org.pegdown" % "pegdown" % "1.6.0" % testScope,
    "org.mockito" % "mockito-core" % "2.10.0" % testScope
  )

  def apply() = compile ++ test
}
