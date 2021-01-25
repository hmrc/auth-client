import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq("com.iheart" %% "ficus" % "1.4.7"),
    play26 = Seq("uk.gov.hmrc" %% "http-verbs-play-26" % "12.3.0" % Provided),
    play27 = Seq("uk.gov.hmrc" %% "http-verbs-play-27" % "12.3.0" % Provided)
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq("org.pegdown" % "pegdown" % "1.6.0" % Test,
                 "org.mockito" % "mockito-core" % "2.10.0" % Test,
                 "org.scalamock" %% "scalamock" % "4.4.0" % Test
    ),
    play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % Test),
    play27 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test)
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
