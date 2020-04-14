/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.auth.core.models

import ai.x.play.json.{Jsonx, SingletonEncoder}
import ai.x.play.json.SingletonEncoder.decodeName
import ai.x.play.json.implicits.formatSingleton
import play.api.libs.json.{Format, JsString}

sealed trait CredentialStrength
case object Weak extends CredentialStrength
case object Strong extends CredentialStrength

object CredentialStrength{
  implicit def simpleNameLowerCase = SingletonEncoder(cls => JsString(decodeName(cls.getSimpleName.toLowerCase)))
  implicit val format: Format[CredentialStrength] = Jsonx.formatSealed[CredentialStrength]
}