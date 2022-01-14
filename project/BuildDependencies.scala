import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq("com.iheart"  %% "ficus"              % "1.4.7"  ),
    play26 = Seq("uk.gov.hmrc" %% "http-verbs-play-26" % "13.11.0" ),
    play27 = Seq("uk.gov.hmrc" %% "http-verbs-play-27" % "13.11.0" ),
    play28 = Seq("uk.gov.hmrc" %% "http-verbs-play-28" % "13.11.0" )
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq("org.scalamock"          %% "scalamock"          % "4.4.0"   % "test, it"),
    play26 = Seq("org.mockito"            %  "mockito-core"       % "2.10.0"  % "test, it",
                 "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"   % "test, it",
                 "org.pegdown"            %  "pegdown"            % "1.6.0"   % "test, it",
                 "uk.gov.hmrc"            %% "bootstrap-backend-play-26" % "5.18.0" % "test, it"),
    play27 = Seq("org.mockito"            %  "mockito-core"       % "2.10.0"  % "test, it",
                 "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"   % "test, it",
                 "org.pegdown"            %  "pegdown"            % "1.6.0"   % "test, it",
                 "uk.gov.hmrc"            %% "bootstrap-backend-play-27" % "5.18.0"  % "test, it"),
    play28 = Seq("com.vladsch.flexmark"   %  "flexmark-all"       % "0.35.10" % "test, it",
                 "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % "test, it",
                 "org.scalatestplus"      %% "mockito-3-4"        % "3.2.2.0" % "test, it",
                 "uk.gov.hmrc"            %% "bootstrap-backend-play-28" % "5.18.0" %  "test, it")
  )


  def apply(): Seq[ModuleID] = compile ++ test
}
