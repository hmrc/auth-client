import PlayCrossCompilation._
import sbt._

private object BuildDependencies {

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq("com.iheart"  %% "ficus"              % "1.4.7"),
    play26 = Seq("uk.gov.hmrc" %% "http-verbs-play-26" % "13.0.0-SNAPSHOT" % Provided),
    play27 = Seq("uk.gov.hmrc" %% "http-verbs-play-27" % "13.0.0-SNAPSHOT" % Provided),
    play28 = Seq("uk.gov.hmrc" %% "http-verbs-play-28" % "13.0.0-SNAPSHOT" % Provided)
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq("org.scalamock"          %% "scalamock"          % "4.4.0"   % Test),
    play26 = Seq("org.mockito"            %  "mockito-core"       % "2.10.0"  % Test,
                 "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"   % Test,
                 "org.pegdown"            %  "pegdown"            % "1.6.0"   % Test),
    play27 = Seq("org.mockito"            %  "mockito-core"       % "2.10.0"  % Test,
                 "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"   % Test,
                 "org.pegdown"            %  "pegdown"            % "1.6.0"   % Test),
    play28 = Seq("com.vladsch.flexmark"   %  "flexmark-all"       % "0.35.10" % Test,
                 "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % Test,
                 "org.scalatestplus"      %% "mockito-3-4"        % "3.2.2.0" % Test)
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
