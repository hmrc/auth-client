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

import play.api.Logging
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.auth.clientversion.ClientVersion
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{CompositeRetrieval, Retrieval}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

trait AuthConnector {
  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A]
}

trait PlayAuthConnector extends AuthConnector with Logging {
  implicit val legacyRawReads: HttpReads[HttpResponse] =
    HttpReads.Implicits.throwOnFailure(HttpReads.Implicits.readEitherOf(HttpReads.Implicits.readRaw))

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

      logIfDeprecated(retrieval) // GG-7679 log a warning if using the deprecated 'name' retrieval

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
          case res @ UpstreamErrorResponse.WithStatusCode(403) =>
            logger.warn(s"GG-7679 service name ${getUserAgent(hc)} generated a Retrievals 403/Forbidden, sessionId: ${hc.sessionId}, requestId: ${hc.requestId}")
            Future.failed(res)
          case res @ UpstreamErrorResponse.WithStatusCode(401) =>
            Future.failed(AuthenticateHeaderParser.parse(res.headers))
        }
    }

  private def getUserAgent(implicit hc: HeaderCarrier): String = {
    hc.otherHeaders.toMap.getOrElse(HeaderNames.USER_AGENT, "No User-Agent")
  }

  private def logIfDeprecated(retrieval: Retrieval[_])(implicit hc: HeaderCarrier): Unit = retrieval match {
    case Retrievals.name =>
      logger.info(s"[GG-7679] service name ${getUserAgent(hc)} used deprecated name Retrieval, sessionId: ${hc.sessionId}, requestId: ${hc.requestId}")
    case CompositeRetrieval(retrievalA, retrievalB) =>
      logIfDeprecated(retrievalA)
      logIfDeprecated(retrievalB)
    case _ => ()
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
