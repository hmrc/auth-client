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

package uk.gov.hmrc.play.json

import play.api.libs.json._

import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

/**
  * Usage examples:
  *
  * Constructor that never fails:
  *
  * {{{
  * case class Foo(value: String)
  *
  * object Foo extends (String => Foo) {
  *
  *   private val mapping = Mappings.map[String, Foo](Foo, _.value)
  *
  *   implicit val jsonFormat = mapping.jsonReads
  *
  *   implicit val pathBindable = mapping.pathBindable
  *
  *   implicit val queryStringBindable = mapping.queryStringBindable
  *
  * }
  * }}}
  *
  * Constructor that may fail:
  *
  * {{{
  * case class Bar(value: String) {
  *   require(value.nonEmpty)
  * }
  *
  * object Bar {
  *
  *   private val mapping = Mappings.mapTry[String, Foo](s => Try(Foo(s)), _.value)
  *
  *   implicit val jsonFormat = mapping.jsonReads
  *
  *   implicit val pathBindable = mapping.pathBindable
  *
  *   implicit val queryStringBindable = mapping.queryStringBindable
  *
  * }
  * }}}
  *
  * Enumeration:
  *
  * {{{
  * sealed trait Base
  * case object Thing1 extends Base
  * case object Thing2 extends Base
  *
  * object Base {
  *
  *   private val mapping = Mappings.mapEnum(Thing1, Thing2)
  *
  *   def fromString(name: String): Option[Base] = mapping.fromString(name)
  *
  *   implicit val jsonFormat = mapping.jsonReads
  *
  *   implicit val pathBindable = mapping.pathBindable
  *
  *   implicit val queryStringBindable = mapping.queryStringBindable
  *
  * }
  * }}}
  */
object Mappings {

  def map[A, B](toDomain: A => B, fromDomain: B => A) = new Mapping[A, B](v => Right(toDomain(v)), fromDomain)

  def mapOption[A, B: ClassTag](toDomain: A => Option[B], fromDomain: B => A): Mapping[A, B] = {
    val errorMessage: A => String = { encoded => s"$encoded could not be mapped to ${classTag[B].runtimeClass.getSimpleName}" }
    new Mapping[A, B](encoded => toDomain(encoded).fold[Either[String, B]](Left(errorMessage(encoded)))(Right(_)), fromDomain)
  }

  def mapEither[A, B](toDomain: A => Either[String, B], fromDomain: B => A) = new Mapping(toDomain, fromDomain)

  def mapTry[A, B](toDomain: A => Try[B], fromDomain: B => A) = new Mapping[A, B](toDomain(_) match {
    case Success(value) => Right(value)
    case Failure(error) => Left(error.getMessage)
  }, fromDomain)

  def mapEnum[B: ClassTag](elements: B*) = new EnumMapping[B](new Enum[B](classTag[B].runtimeClass.getSimpleName, elements))

}


class Mapping[A, B](toDomain: A => Either[String, B], fromDomain: B => A) {


  def jsonReads(implicit base: Reads[A]): Reads[B] = Reads[B] {
    _.validate[A] flatMap {
      encoded => toDomain(encoded).fold[JsResult[B]](JsError(_), JsSuccess(_))
    }
  }

  def jsonWrites(implicit base: Writes[A]): Writes[B] = Writes[B] { domain => base.writes(fromDomain(domain)) }

  def jsonFormat(implicit base: Format[A]): Format[B] = Format(jsonReads(base), jsonWrites(base))

  private def bindToDomain(encoded: Either[String, A]): Either[String, B] = encoded match {
    case Right(value) => toDomain(value)
    case Left(message) => Left(message)
  }

//  def pathBindable(implicit base: PathBindable[A]): PathBindable[B] = new PathBindable[B] {
//
//    def bind(key: String, value: String) = bindToDomain(base.bind(key, value))
//
//    def unbind(key: String, value: B): String = base.unbind(key, fromDomain(value))
//  }
//
//  def queryStringBindable(implicit base: QueryStringBindable[A]): QueryStringBindable[B] = new QueryStringBindable[B] {
//
//    def bind(key: String, params: Map[String, Seq[String]]) = base.bind(key, params).map(bindToDomain)
//
//    def unbind(key: String, value: B) = base.unbind(key, fromDomain(value))
//  }


}

class Enum[B](enumName: String, val elements: Seq[B]) {

  private[json] def fromDomain(element: B): String = {
    val name = element.getClass.getSimpleName
    if (name.endsWith("$")) name.dropRight(1) else name
  }

  private[json] def toDomain(name: String): Either[String, B] = {
    fromName(name).fold[Either[String, B]](Left(s"$name is not an element of the $enumName enumeration"))(Right(_))
  }

  private[this] val elementMap = elements.map(e => (fromDomain(e).toLowerCase, e)).toMap

  def fromName(name: String): Option[B] = elementMap.get(name.toLowerCase)

}

class EnumMapping[B](val enum: Enum[B]) extends Mapping[String, B](enum.toDomain, enum.fromDomain) {

  def fromString(name: String): Option[B] = enum.fromName(name)

}
