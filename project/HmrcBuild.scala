import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc._
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appName = "auth-client"

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.11",
      crossScalaVersions := Seq("2.11.11"),
      libraryDependencies ++= BuildDependencies(),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      ),
      Developers()
    )
}

private object BuildDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "http-core" % "0.6.0" % "provided",
    "net.ceedubs" %% "ficus" % "1.1.2"
  )

  val testScope = "test"
  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % testScope,
    "org.pegdown" % "pegdown" % "1.6.0" % testScope,
    "org.mockito" % "mockito-core" % "2.10.0" % testScope
  )

  def apply() = compile ++ test

}

object Developers {

  def apply() = developers := List[Developer]()
}
