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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * Some type-trickery to allow composing of multiple retrievals at the call site
  */
case class ~[+A, +B](a: A, b: B)

/**
  * A typed class that models what will be returned from auth upon a successful authorisation call
  */
trait Retrieval[A] {

  /** The properties to return from a successful authorise call.
    * These have to be explicitly supported by the auth service being called
    *
    * @return A list of property names to return.
    */
  def propertyNames: Seq[String]

  /** The Reads that will be used upon the returned JSON
    *
    * @return JSON Reads
    */
  def reads: Reads[A]

  /** Allows simpler and more concise code for the lib user at the call site when authorising
    *
    * @param other The Retrieval to be joined with this Retrieval
    * @return A Retrieval[A ~ B] that contains this Retrieval and the one passed in
    */
  def and[B](other: Retrieval[B]): Retrieval[A ~ B] = CompositeRetrieval(this, other)

}

/**
  * A Retrieval for two properties on successful authorisation
  *
  * @param retrievalA
  * @param retrievalB
  */
case class CompositeRetrieval[A, B](retrievalA: Retrieval[A], retrievalB: Retrieval[B])
  extends Retrieval[A ~ B] {

  val propertyNames = retrievalA.propertyNames ++ retrievalB.propertyNames

  lazy val reads: Reads[A ~ B] = (retrievalA.reads and retrievalB.reads) {
    (a, b) => new ~(a, b)
  }

}

/**
  * Simple modelling of a Retrieval that returns nothing (would be a simple authorise-only call)
  */
object EmptyRetrieval extends Retrieval[Unit] {
  val propertyNames = Seq()

  def reads: Reads[Unit] = Reads.pure(())
}

/**
  * A Retrieval that will return only a single property on successful authorisation
  *
  * @param propertyName The name of the property to return from auth on success. Must be a supported property in auth.
  * @param valueReads   The JSON Reads to apply to the returned JSON property
  */
case class SimpleRetrieval[A](propertyName: String, valueReads: Reads[A]) extends Retrieval[A] {
  val reads: Reads[A] = (JsPath \ propertyName).read[A](valueReads)
  val propertyNames = Seq(propertyName)
}

/**
  * A Retrieval that will return only a single, optional property on successful authorisation
  *
  * @param propertyName The name of the property to return from auth on success. Must be a supported property in auth.
  * @param valueReads   The JSON Reads to apply to the returned JSON property in case it is present
  */
case class OptionalRetrieval[A](propertyName: String, valueReads: Reads[A]) extends Retrieval[Option[A]] {
  val reads: Reads[Option[A]] = (JsPath \ propertyName).readNullable[A](valueReads)
  val propertyNames = Seq(propertyName)
}
