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

package uk.gov.hmrc.auth.core

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{OptionalRetrieval, Retrieval, SimpleRetrieval}
import uk.gov.hmrc.play.json.Mappings

import scala.util.{Failure, Success, Try}

sealed abstract class ConfidenceLevel(val level: Int) extends Ordered[ConfidenceLevel] {

  def compare(that: ConfidenceLevel) = this.level.compare(that.level)

  override val toString = level.toString

}

object ConfidenceLevel {

  case object L500 extends ConfidenceLevel(500)

  case object L300 extends ConfidenceLevel(300)

  case object L200 extends ConfidenceLevel(200)

  case object L100 extends ConfidenceLevel(100)

  case object L50 extends ConfidenceLevel(50)

  case object L0 extends ConfidenceLevel(0)

  def fromInt(level: Int): Try[ConfidenceLevel] = level match {
    case 500 => Success(L500)
    case 300 => Success(L300)
    case 200 => Success(L200)
    case 100 => Success(L100)
    case 50 => Success(L50)
    case 0 => Success(L0)
    case _ => Failure(throw new NoSuchElementException(s"Illegal confidence level: $level"))
  }

  private val mapping = Mappings.mapTry[Int, ConfidenceLevel](fromInt, _.level)

  implicit val jsonFormat = mapping.jsonFormat

}

case class CredentialStrength(strength: String) extends Predicate {
  override def toJson: JsValue = Json.obj("credentialStrength" -> strength)
}

object CredentialStrength {

  def strong: String = "strong"

  def weak: String = "weak"
}

case class EnrolmentIdentifier(key: String, value: String)

case class Enrolment(
                      key: String,
                      identifiers: Seq[EnrolmentIdentifier],
                      state: String,
                      confidenceLevel: ConfidenceLevel,
                      delegatedAuthRule: Option[String] = None) extends Predicate {

  def getIdentifier(name: String): Option[EnrolmentIdentifier] = identifiers.find {
    _.key.equalsIgnoreCase(name)
  }

  def isActivated: Boolean = state.toLowerCase == "activated"

  def withConfidenceLevel(confidenceLevel: ConfidenceLevel): Enrolment = copy(confidenceLevel = confidenceLevel)

  def withIdentifier(name: String, value: String): Enrolment =
    copy(identifiers = identifiers :+ EnrolmentIdentifier(name, value))

  def withDelegatedAuthRule(rule: String): Enrolment = copy(delegatedAuthRule = Some(rule))

  def toJson: JsValue = Json.toJson(this)(Enrolment.writes)
}

object Enrolment {
  implicit val idFormat = Json.format[EnrolmentIdentifier]
  implicit val writes = Json.writes[Enrolment].transform { json: JsValue =>
    json match {
      case JsObject(props) => JsObject(props + ("enrolment" -> props("key")) - "key")
    }
  }
  implicit val reads: Reads[Enrolment] = ((__ \ "key").read[String] and
    (__ \ "identifiers").readNullable[Seq[EnrolmentIdentifier]] and
    (__ \ "state").readNullable[String] and
    (__ \ "confidenceLevel").readNullable[ConfidenceLevel] and
    (__ \ "delegatedAuthRule").readNullable[String]) {
    (key, optIds, optState, optCL, optDelegateRule) =>
      Enrolment(
        key,
        optIds.getOrElse(Seq()),
        optState.getOrElse("Activated"),
        optCL.getOrElse(ConfidenceLevel.L0),
        optDelegateRule
      )
  }

  def apply(key: String): Enrolment = apply(key, Seq(), "Activated", ConfidenceLevel.L0, None)
}

case class Enrolments(enrolments: Set[Enrolment]) {

  def getEnrolment(key: String): Option[Enrolment] = enrolments.find(_.key.equalsIgnoreCase(key))

}

trait AffinityGroup extends Predicate {
  def toJson: JsValue = Json.obj("affinityGroup" -> getClass.getSimpleName.dropRight(1))
}

object AffinityGroup {

  case object Individual extends AffinityGroup

  case object Organisation extends AffinityGroup

  case object Agent extends AffinityGroup

  private val mapping = Mappings.mapEnum[AffinityGroup](Individual, Organisation, Agent)

  implicit val jsonFormat = mapping.jsonFormat
}

trait CredentialRole extends Predicate {
  def toJson: JsValue = Json.obj("credentialRole" -> getClass.getSimpleName.dropRight(1).toLowerCase)
}

object CredentialRole {

  case object Admin extends CredentialRole

  case object Assistant extends CredentialRole

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

trait Credentials

case class GGCredId(credId: String) extends Credentials

case class VerifyPid(pid: String) extends Credentials

case class PAClientId(clientId: String) extends Credentials

case object OneTimeLogin extends Credentials

object Credentials {
  val reads: Reads[Credentials] = Reads[Credentials] { json =>

    def toCreds(json: JsLookupResult, f: String => Credentials): Seq[Credentials] = json match {
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

object Retrievals {

  import uk.gov.hmrc.http.controllers.RestFormats.dateTimeRead

  val internalId: Retrieval[Option[String]] = OptionalRetrieval("internalId", Reads.StringReads)
  val externalId: Retrieval[Option[String]] = OptionalRetrieval("externalId", Reads.StringReads)
  val authProviderId: Retrieval[Credentials] = SimpleRetrieval("authProviderId", Credentials.reads)
  val credentialStrength: Retrieval[Option[String]] = OptionalRetrieval("credentialStrength", Reads.StringReads)
  val agentCode: Retrieval[Option[String]] = OptionalRetrieval("agentCode", Reads.StringReads)
  val userDetailsUri: Retrieval[Option[String]] = OptionalRetrieval("userDetailsUri", Reads.StringReads)
  val affinityGroup: Retrieval[Option[AffinityGroup]] = OptionalRetrieval("affinityGroup", AffinityGroup.jsonFormat)
  val loginTimes: Retrieval[LoginTimes] = SimpleRetrieval("loginTimes", Json.reads[LoginTimes])
  val allEnrolments: Retrieval[Enrolments] = SimpleRetrieval("allEnrolments", Reads.set[Enrolment].map(Enrolments))
  val authorisedEnrolments: Retrieval[Enrolments] = SimpleRetrieval("authorisedEnrolments", Reads.set[Enrolment].map(Enrolments))

}
