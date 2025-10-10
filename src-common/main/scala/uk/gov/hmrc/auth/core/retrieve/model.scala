/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.auth.core.retrieve

import play.api.libs.json._
import java.time.Instant

case class Credentials(providerId: String, providerType: String)

object Credentials {
  val reads = Json.reads[Credentials]
}

case class Name(name: Option[String], lastName: Option[String])

object Name {
  val reads = Json.reads[Name]
}

trait LegacyCredentials

case class GGCredId(credId: String) extends LegacyCredentials

case class PAClientId(clientId: String) extends LegacyCredentials

case object OneTimeLogin extends LegacyCredentials

case class StandardApplication(clientId: String) extends LegacyCredentials

object LegacyCredentials {
  val reads: Reads[LegacyCredentials] = Reads[LegacyCredentials] { json =>

    def toCreds(json: JsLookupResult, f: String => LegacyCredentials): Seq[LegacyCredentials] = json match {
      case JsDefined(JsString(value)) => Seq(f(value))
      case _: JsUndefined             => Seq()
      case JsDefined(json)            => throw new RuntimeException(s"Illegal credentials format: ${Json.stringify(json)}")
    }

    toCreds(json \ "ggCredId", GGCredId.apply) ++
      toCreds(json \ "paClientId", PAClientId.apply) ++
      toCreds(json \ "clientId", StandardApplication.apply) ++
      toCreds(json \ "applicationId", StandardApplication.apply) ++ // for backwards compatibility
      toCreds(json \ "oneTimeLogin", _ => OneTimeLogin) match {
        case Seq(creds) => JsSuccess(creds)
        case _          => JsError(s"Illegal format for credentials: ${Json.stringify(json)}")
      }
  }
}

case class LoginTimes(currentLogin: Instant, previousLogin: Option[Instant])

object LoginTimes {
  private implicit val dateTimeReads: Reads[Instant] = Reads.DefaultInstantReads
  val reads: Reads[LoginTimes] = Json.reads[LoginTimes]
}

case class AgentInformation(
    agentId:           Option[String],
    agentCode:         Option[String],
    agentFriendlyName: Option[String])

object AgentInformation {
  val reads: Reads[AgentInformation] = Json.reads[AgentInformation]
}

case class ItmpName(
    givenName:  Option[String],
    middleName: Option[String],
    familyName: Option[String])

object ItmpName {
  val reads: Reads[ItmpName] = Json.reads[ItmpName]
}

case class ItmpAddress(
    line1:       Option[String],
    line2:       Option[String],
    line3:       Option[String],
    line4:       Option[String],
    line5:       Option[String],
    postCode:    Option[String],
    countryName: Option[String],
    countryCode: Option[String])

object ItmpAddress {
  val reads = Json.reads[ItmpAddress]
}

case class MdtpInformation(deviceId: String, sessionId: String)
object MdtpInformation {
  val reads = Json.reads[MdtpInformation]
}

case class GatewayInformation(gatewayToken: Option[String])
object GatewayInformation {
  val reads = Json.reads[GatewayInformation]
}

case class ScpInformation(
    scpSessionId:     Option[String],
    trustId:          Option[String],
    trustIdChangedAt: Option[String],
    trustIdChangedBy: Option[String]
)
