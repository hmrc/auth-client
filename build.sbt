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
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning

val libName = "auth-client"

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello"
  )


lazy val library = Project(libName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory, BuildInfoPlugin)
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
    buildInfoPackage := "buildinfo"
   )
  .settings(ScoverageSettings())
  .settings(SilencerSettings())
  .settings(ScalariformSettings())
