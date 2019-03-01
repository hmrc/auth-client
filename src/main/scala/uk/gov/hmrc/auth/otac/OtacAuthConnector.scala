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

package uk.gov.hmrc.auth.otac

import uk.gov.hmrc.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait OtacAuthorisationResult

object Authorised extends OtacAuthorisationResult

object NoOtacTokenInSession extends OtacAuthorisationResult

object Unauthorised extends OtacAuthorisationResult

case class UnexpectedError(code: Int) extends OtacAuthorisationResult

case class OtacFailureThrowable(result: OtacAuthorisationResult) extends Throwable

trait OtacAuthConnector {
  def authorise(serviceName: String, headerCarrier: HeaderCarrier, otacToken: Option[String]): Future[OtacAuthorisationResult]
}

trait PlayOtacAuthConnector extends OtacAuthConnector {
  val serviceUrl: String

  def http: CoreGet

  def authorise(serviceName: String, headerCarrier: HeaderCarrier, otacToken: Option[String]): Future[OtacAuthorisationResult] =
    otacToken match {
      case Some(ot) => {
        val enhancedHeaderCarrier =
          headerCarrier.withExtraHeaders(HeaderNames.otacAuthorization -> ot)
        callAuth(serviceName, enhancedHeaderCarrier).flatMap(toResult)
      }
      case None => Future.successful(NoOtacTokenInSession)
    }

  private def callAuth[A](serviceName: String, headerCarrier: HeaderCarrier): Future[Int] = {
    implicit val hc = headerCarrier
    http.GET(serviceUrl + s"/authorise/read/$serviceName").map(_.status)
  }

  private def toResult[T](status: Int): Future[OtacAuthorisationResult] =
    status match {
      case 200 => Future.successful(Authorised)
      case 401 => Future.successful(Unauthorised)
      case status => Future.successful(UnexpectedError(status))
    }
}
