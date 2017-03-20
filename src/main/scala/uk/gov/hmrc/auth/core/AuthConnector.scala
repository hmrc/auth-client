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

import play.api.http.{HeaderNames => PlayHeaderNames}
import play.api.libs.json._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait AuthConnector {

  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier): Future[A]

}

trait PlayAuthConnector extends AuthConnector {

  val serviceUrl: String

  def http: HttpPost

  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier): Future[A] = {

    // if the predicate is a single field (1x SimplePredicate), place it into an array
    val predicateJson = predicate.toJson match {
      case arr: JsArray => arr
      case other => Json.arr(other)
    }
    val json = Json.obj(
      "authorise" -> predicateJson,
      "retrieve" -> JsArray(retrieval.propertyNames.map(JsString))
    )
    http.POST(s"$serviceUrl/auth/authorise", json) map {
      _.json match {
        case null => JsNull.as[A](retrieval.reads)
        case bdy => bdy.as[A](retrieval.reads)
      }
    } recoverWith {
      case res@Upstream4xxResponse(_, 401, _, headers) =>
        Future.failed(AuthenticateHeaderParser.parse(headers))
    }
  }

}

object AuthenticateHeaderParser {

  val regex = """^MDTP detail="(.+)"$""".r

  def parse(headers: Map[String, Seq[String]]): AuthorisationException = {
    headers.get(PlayHeaderNames.WWW_AUTHENTICATE).flatMap(_.headOption) match {
      case Some(regex(detail)) => AuthorisationException.fromString(detail)
      case Some(_) => new InternalError("InvalidResponseHeader")
      case None => new InternalError("MissingResponseHeader")
    }
  }

}
