/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.auth.delegation

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class DelegationAuthConnectorSpec extends UnitSpec with MockFactory {

  val hc: HeaderCarrier = HeaderCarrier()

  val ec: ExecutionContextExecutor = ExecutionContext.global

  val delegationContext: DelegationContext = DelegationContext(
    principalName = "Client",
    attorneyName  = "Agent",
    accounts      = TaxIdentifiers(paye = Some(Nino(hasNino = true, Some("AB123456D")))),
    link          = Link(url  = "http://taxplatform/some/dashboard", text = Some("Back to dashboard"))
  )

  val authUrl = "http://localhost:8500"

  val stubResponse = Future.successful(HttpResponse(200))

  val stubbedHttp = stub[WSHttp]

  "DelegationAuthConnector" should {

    "call authoriseDelegation" in {

      (stubbedHttp.POST[DelegationContext, HttpResponse](
        _: String, _: DelegationContext, _: Seq[(String, String)])(
          _: Writes[DelegationContext], _: HttpReads[HttpResponse], _: HeaderCarrier, _: ExecutionContext))
        .when(s"$authUrl/auth/authoriseDelegation", delegationContext, *, *, *, *, *)
        .returns(stubResponse)

      val connector: DelegationAuthConnector = new DelegationAuthConnector {
        override val authServiceUrl = authUrl
        override def http = stubbedHttp
      }

      connector.setDelegation(delegationContext)(hc, ec) should be (stubResponse)
    }

    "call endDelegation" in {

      (stubbedHttp.DELETE[HttpResponse](
        _: String, _: Seq[(String, String)])(
          _: HttpReads[HttpResponse], _: HeaderCarrier, _: ExecutionContext))
        .when(s"$authUrl/auth/endDelegation", *, *, *, *)
        .returns(stubResponse)

      val connector: DelegationAuthConnector = new DelegationAuthConnector {
        override val authServiceUrl = authUrl
        override def http = stubbedHttp
      }

      connector.endDelegation()(hc, ec) should be (stubResponse)
    }

  }

}
