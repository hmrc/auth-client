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

package uk.gov.hmrc.auth.core.authorise

import play.api.libs.json.{JsArray, JsValue, Json}

/**
  * Defines a boolean check against auth properties
  */
trait Predicate {

  /** Convert this predicate to JSON for sending over the wire
    *
    * @return the JSON
    */
  def toJson: JsValue

  /** Join this Predicate with another as a boolean AND
    *
    * @param other A Predicate to join with this Predicate
    * @return A CompositePredicate containing this Predicate and the supplied one
    */
  def and(other: Predicate): Predicate = CompositePredicate(this, other)


  /** Join this Predicate with another as a boolean OR
    *
    * @param other A Predicate to join with this Predicate
    * @return An AlternatePredicate containing this Predicate and the supplied one
    */
  def or(other: Predicate): Predicate = AlternatePredicate(this, other)

}

case class CompositePredicate(predicateA: Predicate, predicateB: Predicate) extends Predicate {

  val toJson = {
    def extractPredicates(predicate: Predicate) = predicate match {
      case CompositePredicate(p1, p2) => Seq(p1, p2) // optimization to avoid unnecessary nesting
      case EmptyPredicate => Seq()
      case other => Seq(other)
    }

    val predicates = extractPredicates(predicateA) ++ extractPredicates(predicateB)
    JsArray(predicates.map(_.toJson))
  }

}

case class AlternatePredicate(predicateA: Predicate, predicateB: Predicate) extends Predicate {

  val toJson = {
    def extractPredicates(predicate: Predicate) = predicate match {
      case AlternatePredicate(p1, p2) => Seq(p1, p2) // optimization to avoid unnecessary nesting
      case EmptyPredicate => Seq()
      case other => Seq(other)
    }

    val predicates = extractPredicates(predicateA) ++ extractPredicates(predicateB)
    Json.obj("$or" -> JsArray(predicates.map(_.toJson)))
  }

}

object EmptyPredicate extends Predicate {
  val toJson = JsArray()
}

case class RawJsonPredicate(toJson: JsArray) extends Predicate
