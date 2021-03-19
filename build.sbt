/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import PlayCrossCompilation._
import sbt.Keys._
import sbt._
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.ServiceManagerPlugin.serviceManagerSettings

val libName = "auth-client"

lazy val externalServices = List(
  ExternalService("AUTH_CLIENT_ALL")
)

lazy val baseDir = file(".")
lazy val absoluteBaseDir = new sbt.File(baseDir.getAbsolutePath.replace(s"${java.io.File.separator}.",""))
lazy val classesDir = absoluteBaseDir / "target"/ "scala-2.12" / "classes"
val jarExclusions = Seq( //exclude the play bits we only needed for integration test
  classesDir/"conf",
  classesDir/"prod",
  classesDir/"application.conf",
  classesDir/"prod.routes"
)

lazy val library = Project(libName, baseDir)
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory, BuildInfoPlugin)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(
    unmanagedSourceDirectories in Compile := (baseDirectory in Compile)(base => Seq(
      base / "src" / "main" / "scala"
    )).value,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(
      base / "src" / "it" / "scala"
    )).value,
    unmanagedSourceDirectories in Test := (baseDirectory in Test)(base => Seq(
      base / "src" / "test" / "scala"
    )).value
  )
  .settings(
    mappings in (Compile, packageBin) ~= { _.filter(mapping => {
      val excludedPath = jarExclusions.find(path=>mapping._1.getAbsolutePath.startsWith(path.getAbsolutePath))
      excludedPath.foreach(_=>println(s"Excluding from jar : ${mapping._1.getAbsolutePath}"))
      excludedPath.isEmpty
    })}
  )
  .settings(serviceManagerSettings: _*)
  .settings(itDependenciesList := externalServices)
  .settings(
    makePublicallyAvailableOnBintray := true,
    majorVersion                     := 5
  )
  .settings(
    name := libName,
    scalaVersion        := "2.12.12",
    libraryDependencies ++= BuildDependencies(),
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    ),
    playCrossCompilationSettings,
    fork in Test := true //Required to prevent https://github.com/sbt/sbt/issues/4609
  )
  .settings( //see https://github.com/sbt/sbt-buildinfo
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "uk.gov.hmrc.auth.clientversion"
   )
  .settings(ScoverageSettings())
  .settings(SilencerSettings())
  .settings(ScalariformSettings())
