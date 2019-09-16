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

import play.api.{Environment, Mode}
import play.api.mvc.{RequestHeader, Results, Result}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}

trait OtacAuthorisationFunctions {
  def authConnector: OtacAuthConnector
  val serviceUrl : String
  val useRelativeRedirect : Boolean = true
  private val tokenParam : String= "p"

  def withVerifiedPasscode[T](serviceName: String, otacToken: Option[String])(body: => Future[T])
                             (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[T] = {

    authConnector.authorise(serviceName, headerCarrier, otacToken).flatMap {
        case Authorised => body
        case otherResult => Future.failed(OtacFailureThrowable(otherResult))
      }
  }

  def withPasscode[_](serviceName: String)(body: => Future[Result])
                          (implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext) = {
    request.session.get(SessionKeys.otacToken).fold(Future.successful(redirectToLogin(request))) { token =>
      withVerifiedPasscode(serviceName, Some(token))(body)
    }
  }

  def redirectToLogin(implicit request: RequestHeader) = {
    val (url, redirectUrl) = if (useRelativeRedirect) {
      (s"/verification/otac/login${tokenQueryParam(request)}", request.path)
    } else {
      (s"$serviceUrl/verification/otac/login${tokenQueryParam(request)}", s"http://${request.host}${request.path}")
    }
    Results.Redirect(url).withNewSession.addingToSession(SessionKeys.redirect -> redirectUrl)
  }

  private[otac] def tokenQueryParam(request: RequestHeader) : String =
    request.getQueryString(tokenParam).map(token => s"?$tokenParam=$token").getOrElse("")
}
