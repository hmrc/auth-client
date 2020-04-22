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

package uk.gov.hmrc.auth.core.models.legacyCredentials

import ai.x.play.json.Jsonx
import ai.x.play.json.SingletonEncoder.simpleName
import ai.x.play.json.implicits.formatSingleton
import play.api.libs.json._

sealed trait LegacyCredentials
final case class GGCredId(ggCredId: String) extends LegacyCredentials
final case class VerifyPid(verifyPid: String) extends LegacyCredentials
final case class PAClientId(paClientId: String) extends LegacyCredentials
final case class StandardApplication(clientId:String) extends LegacyCredentials
case object OneTimeLogin extends LegacyCredentials

object GGCredId{ implicit val format: OFormat[GGCredId] = Json.format[GGCredId] }
object VerifyPid{ implicit val format: OFormat[VerifyPid] = Json.format[VerifyPid] }
object PAClientId{ implicit val format: OFormat[PAClientId] = Json.format[PAClientId] }
object StandardApplication {
  val reads: Reads[StandardApplication] = ((__ \ "clientId").read[String] orElse (__ \ "applicationId").read[String]).map(StandardApplication.apply)
  val writes: OWrites[StandardApplication] = Json.writes[StandardApplication]
  implicit val format: Format[StandardApplication] = Format(reads,writes)
}

object LegacyCredentials {
  implicit val otlFormat : Format[OneTimeLogin.type] = new Format[OneTimeLogin.type] {
    override def writes(o: OneTimeLogin.type): JsValue = Json.obj("oneTimeLogin" → "")
    override def reads(json: JsValue): JsResult[OneTimeLogin.type] = (json \ "oneTimeLogin").toOption.fold[JsResult[OneTimeLogin.type]](
      JsError("Not a OneTimeLogin"))(_ ⇒ JsSuccess(OneTimeLogin)
    )
  }
  implicit val format: Format[LegacyCredentials] = Jsonx.formatSealed[LegacyCredentials]
}