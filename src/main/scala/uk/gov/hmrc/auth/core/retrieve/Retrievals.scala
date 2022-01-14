/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core._
import java.time.{Instant, LocalDate}

@deprecated("Please use uk.gov.hmrc.auth.core.retrieve.v2.Retrievals instead", "2.11.0-play-26")
trait Retrievals {

  val internalId: Retrieval[Option[String]] = OptionalRetrieval("internalId", Reads.StringReads)
  val externalId: Retrieval[Option[String]] = OptionalRetrieval("externalId", Reads.StringReads)
  val credentialStrength: Retrieval[Option[String]] = OptionalRetrieval("credentialStrength", Reads.StringReads)
  val agentCode: Retrieval[Option[String]] = OptionalRetrieval("agentCode", Reads.StringReads)
  @deprecated("Use retrievals that fetch user details data directly as opposed to retrieve json using the uri")
  val userDetailsUri: Retrieval[Option[String]] = OptionalRetrieval("userDetailsUri", Reads.StringReads)
  val affinityGroup: Retrieval[Option[AffinityGroup]] = OptionalRetrieval("affinityGroup", AffinityGroup.jsonFormat)
  val loginTimes: Retrieval[LoginTimes] = SimpleRetrieval("loginTimes", LoginTimes.reads)
  val allEnrolments: Retrieval[Enrolments] = SimpleRetrieval("allEnrolments", Reads.set[Enrolment].map(Enrolments))
  val authorisedEnrolments: Retrieval[Enrolments] = SimpleRetrieval("authorisedEnrolments", Reads.set[Enrolment].map(Enrolments))
  @deprecated("Use 'credentials' retrieval")
  val authProviderId: Retrieval[LegacyCredentials] = SimpleRetrieval("authProviderId", LegacyCredentials.reads)
  val mdtpInformation: Retrieval[Option[MdtpInformation]] = OptionalRetrieval("mdtpInformation", MdtpInformation.reads)
  val gatewayInformation: Retrieval[Option[GatewayInformation]] = OptionalRetrieval("gatewayInformation", GatewayInformation.reads)
  val unreadMessageCount: Retrieval[Option[Int]] = OptionalRetrieval("unreadMessageCount", Reads.IntReads)
  val confidenceLevel: Retrieval[ConfidenceLevel] = SimpleRetrieval("confidenceLevel", ConfidenceLevel.jsonFormat)
  val nino: Retrieval[Option[String]] = OptionalRetrieval("nino", Reads.StringReads)
  val saUtr: Retrieval[Option[String]] = OptionalRetrieval("saUtr", Reads.StringReads)

  val credentials: Retrieval[Credentials] = SimpleRetrieval("credentials", Credentials.reads)
  val name: Retrieval[Name] = SimpleRetrieval("name", Name.reads)
  val dateOfBirth: Retrieval[Option[LocalDate]] = OptionalRetrieval("dateOfBirth", Reads.DefaultLocalDateReads)
  val postCode: Retrieval[Option[String]] = OptionalRetrieval("postCode", Reads.StringReads)
  val email: Retrieval[Option[String]] = OptionalRetrieval("email", Reads.StringReads)
  val description: Retrieval[Option[String]] = OptionalRetrieval("description", Reads.StringReads)
  val agentInformation: Retrieval[AgentInformation] = SimpleRetrieval("agentInformation", AgentInformation.reads)
  val groupIdentifier: Retrieval[Option[String]] = OptionalRetrieval("groupIdentifier", Reads.StringReads)
  val credentialRole: Retrieval[Option[CredentialRole]] = OptionalRetrieval("credentialRole", CredentialRole.reads)

  val allUserDetails = credentials and name and dateOfBirth and postCode and email and
    affinityGroup and agentCode and agentInformation and credentialRole and
    description and groupIdentifier and unreadMessageCount

  val itmpName: Retrieval[ItmpName] = SimpleRetrieval("itmpName", ItmpName.reads)
  val itmpDateOfBirth: Retrieval[Option[LocalDate]] = OptionalRetrieval("itmpDateOfBirth", Reads.DefaultLocalDateReads)
  val itmpAddress: Retrieval[ItmpAddress] = SimpleRetrieval("itmpAddress", ItmpAddress.reads)

  val allItmpUserDetails = itmpName and itmpDateOfBirth and itmpAddress
}

@deprecated("Please use uk.gov.hmrc.auth.core.retrieve.v2.Retrievals instead", "2.11.0-play-26")
object Retrievals extends Retrievals

case class Credentials(providerId: String, providerType: String)

object Credentials {
  val reads = Json.reads[Credentials]
}

case class Name(name: Option[String], lastName: Option[String])

object Name {
  val reads = Json.reads[Name]
}

case class PostCode(value: String)

object PostCode {
  val reads = Json.reads[PostCode]
}

case class Email(value: String)

object Email {
  val reads = Json.reads[Email]
}

trait LegacyCredentials

case class GGCredId(credId: String) extends LegacyCredentials

case class VerifyPid(pid: String) extends LegacyCredentials

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

    toCreds(json \ "ggCredId", GGCredId) ++
      toCreds(json \ "verifyPid", VerifyPid) ++
      toCreds(json \ "paClientId", PAClientId) ++
      toCreds(json \ "clientId", StandardApplication) ++
      toCreds(json \ "applicationId", StandardApplication) ++ // for backwards compatibility
      toCreds(json \ "oneTimeLogin", _ => OneTimeLogin) match {
        case Seq(creds) => JsSuccess(creds)
        case _          => JsError(s"Illegal format for credentials: ${Json.stringify(json)}")
      }
  }
}

case class LoginTimes(currentLogin: Instant, previousLogin: Option[Instant])

object LoginTimes {
  implicit val dateTimeReads = Reads.DefaultInstantReads
  val reads = Json.reads[LoginTimes]
}

case class AgentInformation(agentId:           Option[String],
                            agentCode:         Option[String],
                            agentFriendlyName: Option[String])

object AgentInformation {
  val reads = Json.reads[AgentInformation]
}

case class ItmpName(givenName:  Option[String],
                    middleName: Option[String],
                    familyName: Option[String])

object ItmpName {
  val reads = Json.reads[ItmpName]
}

case class ItmpAddress(line1:       Option[String],
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
