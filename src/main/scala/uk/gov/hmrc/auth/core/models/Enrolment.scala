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

import ai.x.play.json.Jsonx
import ai.x.play.json.SingletonEncoder.simpleName
import ai.x.play.json.implicits.formatSingleton
import play.api.libs.json._
import play.api.libs.functional.syntax._


sealed trait EnrolmentState
case object Activated extends EnrolmentState
case object NotYetActivated extends EnrolmentState

object EnrolmentState {
  implicit val format: Format[EnrolmentState] = Jsonx.formatSealed[EnrolmentState]
}


final case class EnrolmentIdentifier(key: String, value: String)
object EnrolmentIdentifier{ implicit val format = Json.format[EnrolmentIdentifier] }


final case class Enrolment(enrolment: String, identifiers: Seq[EnrolmentIdentifier],
                     state: EnrolmentState, delegatedAuthRule: Option[String] = None)

object Enrolment{

  def apply(key: String): Enrolment = Enrolment(key, Seq(), Activated, None)

  val reads: Reads[Enrolment] = (
      (__ \ "enrolment").read[String].orElse((__ \ "key").read[String]) and
      (__ \ "identifiers").read[Seq[EnrolmentIdentifier]] and
      (__ \ "state").read[EnrolmentState] and
      (__ \ "delegatedAuthRule").readNullable[String]
    )((e, is, s, dar) ⇒ Enrolment.apply(e,is,s,dar))
  val writes: Writes[Enrolment] = Json.writes[Enrolment]

  implicit val format:Format[Enrolment] = Format(reads,writes)


  implicit class EnrolmentOps(e:Enrolment) {


    def getIdentifier(name: String): Option[EnrolmentIdentifier] = e.identifiers.find {
      _.key.equalsIgnoreCase(name)
    }

    def isActivated: Boolean = e.state == Activated

    def withIdentifier(name: String, value: String): Enrolment =
      e.copy(identifiers = e.identifiers :+ EnrolmentIdentifier(name, value))

    def withDelegatedAuthRule(rule: String): Enrolment = e.copy(delegatedAuthRule = Some(rule))

  }

}
