import sbt._

private object BuildDependencies {

  val httpVerbsVersion = "15.0.0"

  val play28 = play("play-28")
  val play29 = play("play-29")
  val play30 = play("play-30")

  private def play(playSuffix: String) = Seq(
    "uk.gov.hmrc"            %% s"http-verbs-$playSuffix"      % httpVerbsVersion,

    "uk.gov.hmrc"            %% s"http-verbs-test-$playSuffix" % httpVerbsVersion                     % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"           % scalaTestPlusPlayVersion(playSuffix) % Test,
    "com.vladsch.flexmark"   %  "flexmark-all"                 % flexmarkAllVersion(playSuffix)       % Test,
    "org.scalatestplus"      %% "mockito-3-4"                  % "3.2.10.0"                           % Test
  )

  private def scalaTestPlusPlayVersion(playSuffix: String): String =
    playSuffix match {
      case "play-28" => "5.1.0"
      case "play-29" => "6.0.0"
      case "play-30" => "7.0.0"
    }

  private def flexmarkAllVersion(playSuffix: String): String =
    playSuffix match {
      case "play-28" => "0.36.8"
      case "play-29" => "0.64.8"
      case "play-30" => "0.64.8"
    }
}
