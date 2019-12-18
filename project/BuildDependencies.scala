import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq("com.iheart" %% "ficus" % "1.4.7"),
    play25 = Seq("uk.gov.hmrc" %% "http-verbs" % "9.8.0-play-25" % Provided),
    play26 = Seq("uk.gov.hmrc" %% "http-verbs" % "9.8.0-play-26" % Provided)
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq("org.pegdown" % "pegdown" % "1.6.0" % Test,
                 "org.mockito" % "mockito-core" % "2.10.0" % Test,
                 "org.scalamock" %% "scalamock" % "4.4.0" % Test
    ),
    play25 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % Test),
    play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test)
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
