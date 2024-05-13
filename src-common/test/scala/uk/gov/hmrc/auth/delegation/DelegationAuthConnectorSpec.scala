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

package uk.gov.hmrc.auth.delegation

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{verify => _, _}
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import org.scalatest.concurrent.IntegrationPatience

class DelegationAuthConnectorSpec extends UnitSpec with WireMockSupport with HttpClientV2Support with IntegrationPatience {

  private lazy val anHttpClientV2 = httpClientV2

  val hc: HeaderCarrier = HeaderCarrier()

  val ec: ExecutionContextExecutor = ExecutionContext.global

  val delegationContext: DelegationContext = DelegationContext(
    principalName = "Client",
    attorneyName  = "Agent",
    accounts      = TaxIdentifiers(paye = Some(Nino(hasNino = true, Some("AB123456D")))),
    link          = Link(url  = "http://taxplatform/some/dashboard", text = Some("Back to dashboard"))
  )

  lazy val connector: DelegationAuthConnector = new DelegationAuthConnector {
    override val authServiceUrl = wireMockUrl
    override val httpClientV2: HttpClientV2 = anHttpClientV2
  }

  "DelegationAuthConnector" should {
    "call authoriseDelegation" in {
      wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/auth/authoriseDelegation"))
          .willReturn(aResponse().withStatus(200))
      )

      connector.setDelegation(delegationContext)(hc, ec).futureValue.status shouldBe 200

      wireMockServer.verify(
        postRequestedFor(urlEqualTo("/auth/authoriseDelegation"))
          .withRequestBody(equalToJson("""{
            "principalName": "Client",
            "attorneyName": "Agent",
            "link" : {
              "url" : "http://taxplatform/some/dashboard",
              "text" : "Back to dashboard"
            },
            "accounts" : {
              "paye" : {
                "hasNino" : true,
                "nino" : "AB123456D"
              }
            }
          }"""))
      )
    }

    "call endDelegation" in {
      wireMockServer.stubFor(
        WireMock.delete(urlEqualTo("/auth/endDelegation"))
          .willReturn(aResponse().withStatus(200))
      )

      connector.endDelegation()(hc, ec).futureValue.status shouldBe 200

      wireMockServer.verify(
        deleteRequestedFor(urlEqualTo("/auth/endDelegation"))
      )
    }
  }
}
