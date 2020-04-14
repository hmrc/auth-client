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

package uk.gov.hmrc.auth.core.predicates

import ai.x.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json, Reads, _}
import uk.gov.hmrc.auth.core.models

/*
 * This class is a superclass over the whole predicate hierarchy
 *
 * [[AndPred]] and [[OrPred]] are recursive types that hold other [[Predicate]] values
 * [[PredicateImpl]] is the superclass for the actual implementation of predicate tests
 *
 * Json formatters are generated automatically for the whole sealed trait via the play-json-extensions library
 *
 *                 +-------------+
 *                 |  Predicate  |
 *                 +-------------+
 *                        |
 *      --------------------------------------
 *      |                 |                  |
 *      v                 v                  v
 * +-----------+     +--------+     +----------------+
 * |  AndPred  |     | OrPred |     | PredicateImpl  |
 * +-----------+     +--------+     +----------------+
 *                                          |
 *                            --------------|----------------------------------...
 *                            |             |               |
 *                            v             v               v
 *                       +--------+   +-----------+  +----------------+
 *                       |  Nino  |   | Enrolment |  |ConfidenceLevel |
 *                       +--------+   +-----------+  +----------------+
 */
sealed trait Predicate extends Product with Serializable
object Predicate {
  implicit val reads: Format[Predicate] = Jsonx.formatSealed[Predicate]

  def innerFormat[Inner:Format,Outer](f : Inner ⇒ Outer, g : Outer ⇒ Inner) : Format[Outer] = {
    val formatA  = implicitly[Format[Inner]]
    val writes = formatA.contramap[Outer](g)
    val reads = formatA.map(f)
    Format(reads,writes)
  }
}

final case class AndPred(and:List[Predicate]) extends Predicate
object AndPred{
  def apply(and: Predicate*): AndPred = new AndPred(and.toList)
  val reads:Reads[AndPred] = JsPath.lazyRead(implicitly[Format[List[Predicate]]]).map(AndPred.apply)
  val writes:Writes[AndPred] =  new Writes[AndPred] {
    override def writes(o: AndPred): JsValue = JsArray(
      o.and.map(p ⇒ Json.toJson(p))
    )
  }
  implicit val format: Format[AndPred] = Format(reads,writes)
}
final case class OrPred($or:List[Predicate]) extends Predicate
object OrPred{
  def apply($or: Predicate*): OrPred = new OrPred($or.toList)
  val reads:Reads[OrPred]    = (__ \ '$or).lazyRead(implicitly[Format[List[Predicate]]]).map(OrPred.apply)
  val writes:Writes[OrPred] = (__ \ '$or).lazyWrite(implicitly[Writes[Seq[Predicate]]]).contramap[OrPred](_.$or)
  implicit val format: Format[OrPred] = Format(reads,writes)
}

/**
  * This trait is the superclass for all actual Predicate implementations
  * Currently the key function is Session -> Boolean, however this is likely to change
  * once we implement predicates based on external information to Seesion -> Future[Boolean]
  * If you don't provide a Json formatter, it will complain at compile time
  */
sealed trait PredicateImpl extends Predicate
object PredicateImpl { implicit val format:Format[PredicateImpl] = Jsonx.formatSealed[PredicateImpl] }

final case class Nino(hasNino:Boolean, nino:Option[models.Nino]) extends PredicateImpl
object Nino {
  implicit val format: OFormat[Nino] = (
    (JsPath \ "hasNino").format[Boolean] ~
      (JsPath \ "nino").formatNullable[String].inmap[Option[models.Nino]](s ⇒ s.map(models.Nino.apply),n ⇒ n.map(_.nino))
    )(Nino.apply,unlift(Nino.unapply))
}

final case class Enrolment(enrolment:models.Enrolment) extends PredicateImpl
object Enrolment {
  def apply(s:String) = new Enrolment(models.Enrolment(s))
  implicit val format: Format[Enrolment] = Predicate.innerFormat[models.Enrolment,Enrolment](Enrolment.apply,_.enrolment)
}

final case class ConfidenceLevel(confidenceLevel: models.ConfidenceLevel) extends PredicateImpl
object ConfidenceLevel {
  implicit val format: Format[ConfidenceLevel] = Predicate.innerFormat[models.ConfidenceLevel,ConfidenceLevel](ConfidenceLevel.apply,_.confidenceLevel)
}


final case class CredentialStrength(credentialStrength: models.CredentialStrength) extends PredicateImpl
object CredentialStrength{ implicit val format: OFormat[CredentialStrength] = Json.format[CredentialStrength] }

final case class AuthProviders(authProviders: models.AuthProvider*) extends PredicateImpl
object AuthProviders{ implicit val format: OFormat[AuthProviders] = Json.format[AuthProviders] }

final case class AffinityGroup(affinityGroup: models.AffinityGroup) extends PredicateImpl
object AffinityGroup{ implicit val format: OFormat[AffinityGroup] = Json.format[AffinityGroup] }

final case class CredentialRole(credentialRole: models.CredentialRole) extends PredicateImpl
object CredentialRole{ implicit val format: OFormat[CredentialRole] = Json.format[CredentialRole] }

final case class Relationship(relationship: models.Relationship) extends PredicateImpl
object Relationship{
  implicit val format: OFormat[Relationship] =  new OFormat[Relationship] {
    override def writes(o: Relationship): JsObject = Json.toJsObject(o.relationship)
    override def reads(json: JsValue): JsResult[Relationship] = json.validate[models.Relationship].map(Relationship.apply)
  }
}

final case class EmptyPredicate() extends PredicateImpl
object EmptyPredicate{
  val writes: Writes[EmptyPredicate] = new Writes[EmptyPredicate] {
    override def writes(o: EmptyPredicate): JsValue = JsArray()
  }
  val reads: Reads[EmptyPredicate] = new Reads[EmptyPredicate] {
    override def reads(json: JsValue): JsResult[EmptyPredicate] = json match {
      case JsArray(x) if x.isEmpty ⇒ JsSuccess(EmptyPredicate())
      case other                   ⇒ JsError(s"Not an EmptyPredicate: $other")
    }
  }

  implicit val format: Format[EmptyPredicate] = Format(reads,writes)
}