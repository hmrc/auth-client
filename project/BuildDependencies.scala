import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val httpVerbsVersion = "14.8.0"

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq("com.iheart"  %% "ficus"              % "1.5.2"         ),
    play28 = Seq("uk.gov.hmrc" %% "http-verbs-play-28" % httpVerbsVersion)
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq("org.scalamock"          %% "scalamock"          % "5.2.0"   % "test, it"),
    play28 = Seq("com.vladsch.flexmark"   %  "flexmark-all"       % "0.36.8" % "test, it",
                 "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % "test, it",
                 "org.scalatestplus"      %% "mockito-3-4"        % "3.2.10.0" % "test, it",
                 "uk.gov.hmrc"            %% "http-verbs-test-play-28" % httpVerbsVersion %  "test, it")
  )


  def apply(): Seq[ModuleID] = compile ++ test
}
