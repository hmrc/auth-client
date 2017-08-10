/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.authorise.Predicate

object Retrievals {

  import uk.gov.hmrc.http.controllers.RestFormats.dateTimeRead
  import uk.gov.hmrc.http.controllers.RestFormats.localDateRead

  val internalId: Retrieval[Option[String]] = OptionalRetrieval("internalId", Reads.StringReads)
  val externalId: Retrieval[Option[String]] = OptionalRetrieval("externalId", Reads.StringReads)
  val credentialStrength: Retrieval[Option[String]] = OptionalRetrieval("credentialStrength", Reads.StringReads)
  val agentCode: Retrieval[Option[String]] = OptionalRetrieval("agentCode", Reads.StringReads)
  val userDetailsUri: Retrieval[Option[String]] = OptionalRetrieval("userDetailsUri", Reads.StringReads)
  val affinityGroup: Retrieval[Option[AffinityGroup]] = OptionalRetrieval("affinityGroup", AffinityGroup.jsonFormat)
  val loginTimes: Retrieval[LoginTimes] = SimpleRetrieval("loginTimes", Json.reads[LoginTimes])
  val allEnrolments: Retrieval[Enrolments] = SimpleRetrieval("allEnrolments", Reads.set[Enrolment].map(Enrolments))
  val authorisedEnrolments: Retrieval[Enrolments] = SimpleRetrieval("authorisedEnrolments", Reads.set[Enrolment].map(Enrolments))
  val authProviderId: Retrieval[LegacyCredentials] = SimpleRetrieval("authProviderId", LegacyCredentials.reads)

  val credentials: Retrieval[Credentials] = SimpleRetrieval("credentials", Credentials.reads)
  val name: Retrieval[Name] = SimpleRetrieval("name", Name.reads)
  val dateOfBirth: Retrieval[Option[LocalDate]] = OptionalRetrieval("dateOfBirth", localDateRead)
  val postCode: Retrieval[Option[String]] = OptionalRetrieval("postCode", Reads.StringReads)
  val email: Retrieval[Option[String]] = OptionalRetrieval("email", Reads.StringReads)
  val description: Retrieval[Option[String]] = OptionalRetrieval("description", Reads.StringReads)
  val agentInformation: Retrieval[AgentInformation] = SimpleRetrieval("agentInformation", AgentInformation.reads)
  val groupIdentifier: Retrieval[Option[String]] = OptionalRetrieval("groupIdentifier", Reads.StringReads)
  val credentialRole: Retrieval[Option[CredentialRole]] = OptionalRetrieval("credentialRole", CredentialRole.reads)

  val allUserDetails = credentials and name and dateOfBirth and postCode and email and
    affinityGroup and agentCode and agentInformation and credentialRole and
    description and groupIdentifier

}

trait AuthProvider

  case object AuthProvider {

  object GovernmentGateway extends AuthProvider

  object Verify extends AuthProvider

  object OneTimeLogin extends AuthProvider

  object PrivilegedApplication extends AuthProvider

}

  case class AuthProviders(providers: AuthProvider*) extends Predicate {
  def toJson: JsValue = Json.obj("authProviders" -> providers.map(_.getClass.getSimpleName.dropRight(1)))
}

  case class Credentials(providerId: String, providerType: String)

  object Credentials {
  implicit val reads = Json.reads[Credentials]
}

  case class Name(name: Option[String], lastName: Option[String])

object Name {
  implicit val reads = Json.reads[Name]
}

case class PostCode(value: String)

object PostCode {
  implicit val reads = Json.reads[PostCode]
}

case class Email(value: String)

object Email {
  implicit val reads = Json.reads[Email]
}

trait LegacyCredentials

case class GGCredId(credId: String) extends LegacyCredentials

case class VerifyPid(pid: String) extends LegacyCredentials

case class PAClientId(clientId: String) extends LegacyCredentials

case object OneTimeLogin extends LegacyCredentials

object LegacyCredentials {
  val reads: Reads[LegacyCredentials] = Reads[LegacyCredentials] { json =>

    def toCreds(json: JsLookupResult, f: String => LegacyCredentials): Seq[LegacyCredentials] = json match {
      case JsDefined(JsString(value)) => Seq(f(value))
      case _: JsUndefined => Seq()
      case JsDefined(json) => throw new RuntimeException(s"Illegal credentials format: ${Json.stringify(json)}")
    }

    toCreds(json \ "ggCredId", GGCredId) ++ toCreds(json \ "verifyPid", VerifyPid) ++
      toCreds(json \ "paClientId", PAClientId) ++ toCreds(json \ "oneTimeLogin", _ => OneTimeLogin) match {
      case Seq(creds) => JsSuccess(creds)
      case _ => JsError(s"Illegal format for credentials: ${Json.stringify(json)}")
    }
  }
}

case class LoginTimes(currentLogin: DateTime, previousLogin: Option[DateTime])

sealed trait CredentialRole

object Admin extends CredentialRole

object Assistant extends CredentialRole

object CredentialRole {
    implicit val reads: Reads[CredentialRole] = new Reads[CredentialRole] {
      override def reads(json: JsValue): JsResult[CredentialRole] =
        json.as[String].toLowerCase match {
          case "admin" => JsSuccess(Admin)
          case "assistant" => JsSuccess(Assistant)
          case value => JsError("Unsupported credential role value: " + value)
        }
    }
  }

case class AgentInformation(agentId: Option[String],
                              agentCode: Option[String],
                              agentFriendlyName: Option[String])

object AgentInformation {
    implicit val reads = Json.reads[AgentInformation]
  }
