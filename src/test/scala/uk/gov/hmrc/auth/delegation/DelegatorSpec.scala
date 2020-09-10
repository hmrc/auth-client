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

package uk.gov.hmrc.auth.delegation

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Ok
import play.api.mvc.{RequestHeader, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DelegatorSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val DelegationStateSessionKey = "delegationState"

  val mockDelegationAuthConnector: DelegationAuthConnector = mock[DelegationAuthConnector]

  val delegator: Delegator = new Delegator {
    override protected def delegationConnector: DelegationAuthConnector = mockDelegationAuthConnector
  }

  "The startDelegation method" should {

    "pass the delegation data to the connector and return a redirect response with the delegation session flag" in {

      implicit val request: RequestHeader = FakeRequest()

      val delegationContext = DelegationContext(
        principalName = "Dave Client",
        attorneyName  = "Bob Agent",
        accounts      = TaxIdentifiers(paye = Some(Nino(hasNino = true, Some("AB123456D")))),
        link          = Link(url  = "http://taxplatform/some/dashboard", text = Some("Back to dashboard"))
      )

      val redirectTo = "http://blah/blah"

      when(mockDelegationAuthConnector.setDelegation(delegationContext)).thenReturn(Future.successful(HttpResponse(201)))

      val result: Result = await(delegator.startDelegationAndRedirect(delegationContext, redirectTo))

      result.header.status shouldBe 303
      result.header.headers.get("Location") shouldBe Some("http://blah/blah")

      result.session.get(DelegationStateSessionKey) shouldBe Some("On")

      verify(mockDelegationAuthConnector).setDelegation(delegationContext)
    }
  }

  "The endDelegation method" should {

    "call the delegation connector to end delegation and remove the delegation session flag" in {

      implicit val request: RequestHeader =
        FakeRequest().withSession(DelegationStateSessionKey -> "On")

      assert(request.session.get(DelegationStateSessionKey).contains("On"))

      when(mockDelegationAuthConnector.endDelegation).thenReturn(Future.successful(HttpResponse(204)))

      val result = await(delegator.endDelegation(Ok))

      result.session.get(DelegationStateSessionKey) shouldBe None

      result.header.status shouldBe 200

      verify(mockDelegationAuthConnector).endDelegation()
    }
  }

}
