import sbt._

private object BuildDependencies {

  val httpVerbsVersion = "14.11.0-SNAPSHOT"

  val shared = Seq(
    "com.iheart"             %% "ficus"              % "1.5.2",

    "org.scalamock"          %% "scalamock"          % "5.2.0"   % Test
  )

  val play28 = Seq(
    "uk.gov.hmrc"            %% "http-verbs-play-28"      % httpVerbsVersion,

    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.36.8"         % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"          % Test,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"       % Test,
    "uk.gov.hmrc"            %% "http-verbs-test-play-28" % httpVerbsVersion % Test
  )

  val play29 = Seq(
    "uk.gov.hmrc"            %% "http-verbs-play-29"      % httpVerbsVersion,

    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.62.2"         % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "6.0.0-RC2"      % Test,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"       % Test,
    "uk.gov.hmrc"            %% "http-verbs-test-play-29" % httpVerbsVersion % Test
  )
}
