import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc._
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appName = "play-auth"

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.7",
      crossScalaVersions := Seq("2.11.7"),
      libraryDependencies ++= BuildDependencies(),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      ),
      Developers()
    )
}

private object BuildDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current % "provided",
    ws % "provided",
    "uk.gov.hmrc" %% "http-verbs" % "6.3.0" % "provided",
    "net.ceedubs" %% "ficus" % "1.1.1"
  )

  val testScope = "test"
  val test = Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % testScope,
    "org.pegdown" % "pegdown" % "1.5.0" % testScope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % testScope
  )

  def apply() = compile ++ test

}

object Developers {

  def apply() = developers := List[Developer]()
}
