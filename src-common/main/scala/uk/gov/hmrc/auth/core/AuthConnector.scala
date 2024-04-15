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

package uk.gov.hmrc.auth.core

import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.auth.clientversion.ClientVersion
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, Upstream4xxResponse}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

trait AuthConnector {
  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A]
}

trait PlayAuthConnector extends AuthConnector {

  val serviceUrl: String

  def httpClientV2: HttpClientV2

  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    hc.authorization.fold[Future[A]](Future.failed(new MissingBearerToken)) { _ =>
      // if the predicate is a single field (1x SimplePredicate), place it into an array
      val predicateJson = predicate.toJson match {
        case arr: JsArray => arr
        case other        => Json.arr(other)
      }
      val json = Json.obj(
        "authorise" -> predicateJson,
        "retrieve" -> JsArray(retrieval.propertyNames.map(JsString.apply)))

      httpClientV2
        .post(url"$serviceUrl/auth/authorise")
        .setHeader("Auth-Client-Version" -> ClientVersion.toString)
        .withBody(json)
        .execute[HttpResponse]
        .map { res =>
          res.json match {
            case null => JsNull.as[A](retrieval.reads)
            case bdy  => bdy.as[A](retrieval.reads)
          }
        }.recoverWith {
          case res @ Upstream4xxResponse(_, 401, _, headers) =>
            Future.failed(AuthenticateHeaderParser.parse(headers))
        }
    }
}

object AuthenticateHeaderParser {

  val WWW_AUTHENTICATE = "WWW-Authenticate"
  val ENROLMENT = "Failing-Enrolment"
  val regex = """^MDTP detail="(.+)"$""".r

  def parse(headers: Map[String, Seq[String]]): AuthorisationException = {
    headers.get(WWW_AUTHENTICATE).flatMap(_.headOption) match {
      case Some(regex(detail)) => AuthorisationException.fromString(detail) match {
        case ie: InsufficientEnrolments => headers.get(ENROLMENT).flatMap(_.headOption).fold(ie)(e => ie.copy(msg = e))
        case other                      => other
      }
      case Some(_) => new InternalError("InvalidResponseHeader")
      case None    => new InternalError("MissingResponseHeader")
    }
  }
}
