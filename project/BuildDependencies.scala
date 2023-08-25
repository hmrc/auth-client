import sbt._

private object BuildDependencies {

  val httpVerbsVersion = "14.8.0-SNAPSHOT"

  val shared = Seq(
    "com.iheart"             %% "ficus"              % "1.5.2",

    "org.scalamock"          %% "scalamock"          % "5.2.0"   % "test, it"
  )

  val play28 = Seq(
    "uk.gov.hmrc"            %% "http-verbs-play-28" % httpVerbsVersion,

    "com.vladsch.flexmark"   %  "flexmark-all"       % "0.36.8"   % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"    % "test, it",
    "org.scalatestplus"      %% "mockito-3-4"        % "3.2.10.0" % "test, it",
    "uk.gov.hmrc"            %% "http-verbs-test-play-28" % httpVerbsVersion %  "test, it"
  )

  val play29 = Seq(
    "uk.gov.hmrc"            %% "http-verbs-play-29" % httpVerbsVersion,

    "com.vladsch.flexmark"   %  "flexmark-all"       % "0.62.2"   % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-M6" % "test, it",
    "org.scalatestplus"      %% "mockito-3-4"        % "3.2.10.0" % "test, it",
    "uk.gov.hmrc"            %% "http-verbs-test-play-29" % httpVerbsVersion %  "test, it"
  )
}
