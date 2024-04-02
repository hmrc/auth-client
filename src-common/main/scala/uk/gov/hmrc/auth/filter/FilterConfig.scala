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

package uk.gov.hmrc.auth.filter

import play.api.Configuration
import com.typesafe.config.{Config, ConfigRenderOptions}

case class AuthConfig(patterns: Seq[String], predicates: Seq[Config]) {

  val pathMatchers = patterns.map(PathMatcher.apply)

  val predicatesAsJson = predicates
    .map(_.root.render(ConfigRenderOptions.concise))
    .mkString("[", ",", "]")

}

case class FilterConfig(controllerConfigs: Configuration) {

  private def toAuthConfig(config: Configuration): AuthConfig =
    AuthConfig(
      patterns   = config.getOptional[Seq[String]]("patterns").getOrElse(Seq.empty),
      predicates = config.getOptional[Seq[Config]]("predicates").getOrElse(Seq.empty)
    )

  private val presets: Map[String, AuthConfig] =
    controllerConfigs.getOptional[Map[String, Configuration]]("authorisation")
      .fold(Map.empty[String, AuthConfig])(_.mapValues(toAuthConfig).toMap)

  def getConfigByName(name: String): AuthConfig =
    presets.getOrElse(name, throw new RuntimeException(s"unknown auth config: '$name'")) // TODO - error handling might get improved

  def getConfigForController(controller: String): Seq[AuthConfig] =
    controllerConfigs.getOptional[Seq[String]](s"$controller.authorisedBy").fold(Seq.empty[AuthConfig])(_.map(getConfigByName))

}
