/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.auth.clientversion

import uk.gov.hmrc.http.BuildInfo

/**
 * returns auth-client-x.x.x string as derived using the sbt-buildinfo plugin and associated build.sbt settings.
 * (See https://github.com/sbt/sbt-buildinfo)
 */
object ClientVersion {
  private val versionRegex = raw"([0-9]+\.[0-9]+\.[0-9]+).*".r

  val version = BuildInfo.version match {
    case versionRegex(version) => version
    case _                     => throw new RuntimeException(s"auth-client version could not be determined from BuildInfo.version : ${BuildInfo.version}")
  }

  val name = "auth-client"

  override def toString() =
    s"$name-$version"
}
