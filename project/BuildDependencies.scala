import sbt._

private object BuildDependencies {

  val httpVerbsVersion = "15.5.0"

  def dependenciesFor(playSuffix: String) = Seq(
    "uk.gov.hmrc"            %% s"http-verbs-$playSuffix"      % httpVerbsVersion,

    "uk.gov.hmrc"            %% s"http-verbs-test-$playSuffix" % httpVerbsVersion                     % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"           % scalaTestPlusPlayVersion(playSuffix) % Test,
    "com.vladsch.flexmark"   %  "flexmark-all"                 % flexmarkAllVersion(playSuffix)       % Test,
    "org.scalatestplus"      %% "mockito-4-11"                 % "3.2.18.0"                           % Test
  )

  private def scalaTestPlusPlayVersion(playSuffix: String): String =
    playSuffix match {
      case "play-30" => "7.0.1"
    }

  private def flexmarkAllVersion(playSuffix: String): String =
    playSuffix match {
      case "play-30" => "0.64.8"
    }
}
