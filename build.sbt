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

import sbt.Keys.*
import sbt.*
import uk.gov.hmrc.DefaultBuildSettings

val scala2_13 = "2.13.12"
val scala3    = "3.3.4"

ThisBuild / majorVersion     := 8
ThisBuild / isPublicArtefact := true
ThisBuild / scalaVersion     := scala2_13
ThisBuild / Test / fork      := true //Required to prevent https://github.com/sbt/sbt/issues/4609

lazy val library = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(
    authClient,
    authClientPlay29,
    authClientPlay30
  )

// empty artefact, exists to ensure eviction of previous auth-client jar which has now moved into auth-client-play-28
lazy val authClient = Project("auth-client", file("auth-client"))

val sharedSources = Seq(
  Compile         / unmanagedSourceDirectories   += baseDirectory.value / s"../src-common/main/scala",
  Compile         / unmanagedResourceDirectories += baseDirectory.value / s"../src-common/main/resources",
  Test            / unmanagedSourceDirectories   += baseDirectory.value / s"../src-common/it/scala",
  Test            / unmanagedSourceDirectories   += baseDirectory.value / s"../src-common/test/scala",
  Test            / unmanagedResourceDirectories += baseDirectory.value / s"../src-common/test/resources"
)

/**
 * declared as a module so that the IDE can resolve dependencies and allow easier development
 * Should generally depend on the latest play version deps
 */
lazy val srcCommon = Project("src-common", file("src-common"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++= BuildDependencies.play30,
    sharedSources
    )
  .settings( //see https://github.com/sbt/sbt-buildinfo
             buildInfoKeys    := Seq[BuildInfoKey](name, version),
             buildInfoPackage := "uk.gov.hmrc.auth.clientversion"
             )
  .settings(ScoverageSettings())
  .settings(ScalariformSettings())

lazy val authClientPlay29 = Project("auth-client-play-29", file("auth-client-play-29"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++= BuildDependencies.play29,
    sharedSources
  )
  .settings( //see https://github.com/sbt/sbt-buildinfo
    buildInfoKeys    := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "uk.gov.hmrc.auth.clientversion"
  )
  .settings(ScoverageSettings())
  .settings(ScalariformSettings())

lazy val authClientPlay30 = Project("auth-client-play-30", file("auth-client-play-30"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    crossScalaVersions := Seq(scala2_13, scala3),
    libraryDependencies ++= BuildDependencies.play30,
    sharedSources
  )
  .settings( //see https://github.com/sbt/sbt-buildinfo
    buildInfoKeys    := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "uk.gov.hmrc.auth.clientversion"
  )
  .settings(ScoverageSettings())
  .settings(ScalariformSettings())

// Run `sm2 --start AUTH_CLIENT_ALL` before `sbt it/test`
lazy val it = (project in file("it"))
  .settings(publish / skip := true)
  .aggregate(
    itPlay29,
    itPlay30
  )

lazy val itPlay29 = Project("it-play-29", file("it-play-29"))
  .settings(DefaultBuildSettings.itSettings())
  .settings(Test / unmanagedSourceDirectories += baseDirectory.value / s"../src-common/it/scala")
  .dependsOn(authClientPlay29 % "test->test")

lazy val itPlay30 = Project("it-play-30", file("it-play-30"))
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    crossScalaVersions := Seq(scala2_13, scala3),
    Test / unmanagedSourceDirectories += baseDirectory.value / s"../src-common/it/scala"
  )
  .dependsOn(authClientPlay30 % "test->test")
