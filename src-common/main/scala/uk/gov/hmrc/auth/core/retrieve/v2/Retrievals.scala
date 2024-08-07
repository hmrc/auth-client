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

package uk.gov.hmrc.auth.core.retrieve.v2

import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._

import java.time.LocalDate

trait Retrievals {
  val internalId: Retrieval[Option[String]] = OptionalRetrieval("internalId", Reads.StringReads)
  val externalId: Retrieval[Option[String]] = OptionalRetrieval("externalId", Reads.StringReads)
  val credentialStrength: Retrieval[Option[String]] = OptionalRetrieval("credentialStrength", Reads.StringReads)
  val agentCode: Retrieval[Option[String]] = OptionalRetrieval("agentCode", Reads.StringReads)
  @deprecated("Use retrievals that fetch user details data directly as opposed to retrieve json using the uri", since = "2.15.0")
  val userDetailsUri: Retrieval[Option[String]] = OptionalRetrieval("userDetailsUri", Reads.StringReads)
  val affinityGroup: Retrieval[Option[AffinityGroup]] = OptionalRetrieval("affinityGroup", AffinityGroup.jsonFormat)
  val loginTimes: Retrieval[LoginTimes] = SimpleRetrieval("loginTimes", LoginTimes.reads)
  val allEnrolments: Retrieval[Enrolments] = SimpleRetrieval("allEnrolments", Reads.set[Enrolment].map(Enrolments.apply))
  val authorisedEnrolments: Retrieval[Enrolments] = SimpleRetrieval("authorisedEnrolments", Reads.set[Enrolment].map(Enrolments.apply))
  @deprecated("Use 'credentials' retrieval", since = "2.15.0")
  val authProviderId: Retrieval[LegacyCredentials] = SimpleRetrieval("authProviderId", LegacyCredentials.reads)
  val mdtpInformation: Retrieval[Option[MdtpInformation]] = OptionalRetrieval("mdtpInformation", MdtpInformation.reads)
  val gatewayInformation: Retrieval[Option[GatewayInformation]] = OptionalRetrieval("gatewayInformation", GatewayInformation.reads)
  val confidenceLevel: Retrieval[ConfidenceLevel] = SimpleRetrieval("confidenceLevel", ConfidenceLevel.jsonFormat)
  val nino: Retrieval[Option[String]] = OptionalRetrieval("nino", Reads.StringReads)
  val saUtr: Retrieval[Option[String]] = OptionalRetrieval("saUtr", Reads.StringReads)

  val dateOfBirth: Retrieval[Option[LocalDate]] = OptionalRetrieval("dateOfBirth", Reads.DefaultLocalDateReads)
  val postCode: Retrieval[Option[String]] = OptionalRetrieval("postCode", Reads.StringReads)
  val email: Retrieval[Option[String]] = OptionalRetrieval("email", Reads.StringReads)
  val description: Retrieval[Option[String]] = OptionalRetrieval("description", Reads.StringReads)
  val groupIdentifier: Retrieval[Option[String]] = OptionalRetrieval("groupIdentifier", Reads.StringReads)
  val credentialRole: Retrieval[Option[CredentialRole]] = OptionalRetrieval("credentialRole", CredentialRole.reads)
  val agentInformation: Retrieval[AgentInformation] = SimpleRetrieval("agentInformation", AgentInformation.reads)

  val credentials: Retrieval[Option[Credentials]] = OptionalRetrieval("optionalCredentials", Credentials.reads) // eg: providerType = "GovernmentGateway", providerId = <credId> | <authProviderId>

  // Deprecated as at 2024-04, see Tech Blog Post dated 2024-02-26
  @deprecated("See Tech Blog Post dated 2024-02-26.", since = "8.2.0")
  val name: Retrieval[Option[Name]] = OptionalRetrieval("optionalName", Name.reads)
  val itmpName: Retrieval[Option[ItmpName]] = OptionalRetrieval("optionalItmpName", ItmpName.reads)
  val itmpAddress: Retrieval[Option[ItmpAddress]] = OptionalRetrieval("optionalItmpAddress", ItmpAddress.reads)

  val itmpDateOfBirth: Retrieval[Option[LocalDate]] = OptionalRetrieval("itmpDateOfBirth", Reads.DefaultLocalDateReads)

  val allUserDetails = credentials and name and dateOfBirth and postCode and email and
    affinityGroup and agentCode and agentInformation and credentialRole and
    description and groupIdentifier

  val allItmpUserDetails = itmpName and itmpDateOfBirth and itmpAddress

  val profile: Retrieval[Option[String]] = OptionalRetrieval("profile", Reads.StringReads)
  val groupProfile: Retrieval[Option[String]] = OptionalRetrieval("groupProfile", Reads.StringReads)
  val emailVerified: Retrieval[Option[Boolean]] = OptionalRetrieval("emailVerified", Reads.BooleanReads)

  val oauthTokens: Retrieval[Option[OauthTokens]] = OptionalRetrieval("oauthTokens", OauthTokens.reads)

  val trustedHelper: Retrieval[Option[TrustedHelper]] = OptionalRetrieval("trustedHelper", TrustedHelper.reads)

  val legacySaUserId: Retrieval[Option[String]] = OptionalRetrieval("legacySaUserId", Reads.StringReads)

  val clientId: Retrieval[Option[String]] = OptionalRetrieval("clientId", Reads.StringReads)

  val applicationName: Retrieval[Option[String]] = OptionalRetrieval("applicationName", Reads.StringReads)

  val applicationId: Retrieval[Option[String]] = OptionalRetrieval("applicationId", Reads.StringReads)

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val scpSessionId: Retrieval[Option[String]] = OptionalRetrieval("scpSessionId", Reads.StringReads)

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val trustId: Retrieval[Option[String]] = OptionalRetrieval("trustId", Reads.StringReads)

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val trustIdChangedAt: Retrieval[Option[String]] = OptionalRetrieval("trustIdChangedAt", Reads.StringReads)

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val trustIdChangedBy: Retrieval[Option[String]] = OptionalRetrieval("trustIdChangedBy", Reads.StringReads)

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val scpInformation: Retrieval[ScpInformation] = new Retrieval[ScpInformation] {
    def propertyNames: Seq[String] = Seq("scpInformation")
    def reads: Reads[ScpInformation] = (scpSessionId and trustId and trustIdChangedAt and trustIdChangedBy).reads.map {
      case a ~ b ~ c ~ d => ScpInformation(a, b, c, d)
    }
  }

  // An allowlisted Retrieval, only useable by selected internal services, returns 403/Forbidden otherwise.
  val identityProviderType: Retrieval[Option[String]] = OptionalRetrieval("identityProviderType", Reads.StringReads) // eg: "SCP" or "ONE_LOGIN"
}

object Retrievals extends Retrievals

case class OauthTokens(accessToken: Option[String], refreshToken: Option[String], idToken: Option[String])

object OauthTokens {
  val reads: Reads[OauthTokens] = Json.reads[OauthTokens]
}

case class TrustedHelper(principalName: String, attorneyName: String, returnLinkUrl: String, principalNino: Option[String])

object TrustedHelper {
  val reads: Reads[TrustedHelper] = Json.reads[TrustedHelper]
}
