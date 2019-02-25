/*
 * Copyright 2019 HM Revenue & Customs
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

import com.typesafe.config.{Config, ConfigRenderOptions}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._


case class AuthConfig(patterns: Seq[String], predicates: Seq[Config]) {

  val pathMatchers = patterns.map(PathMatcher)

  val predicatesAsJson = predicates
    .map(_.root.render(ConfigRenderOptions.concise))
    .mkString("[", ",", "]")

}


case class FilterConfig(controllerConfigs: Config) {

  private val presets: Map[String, AuthConfig] =
    controllerConfigs.as[Option[Map[String, AuthConfig]]]("authorisation").getOrElse(Map())

  def getConfigByName(name: String): AuthConfig =
    presets.getOrElse(name, throw new RuntimeException(s"unknown auth config: '$name'")) // TODO - error handling might get improved

  def getConfigForController(controller: String): Seq[AuthConfig] =
    controllerConfigs.getAs[Seq[String]](s"$controller.authorisedBy").map(_.map(getConfigByName)).getOrElse(Seq())

}
