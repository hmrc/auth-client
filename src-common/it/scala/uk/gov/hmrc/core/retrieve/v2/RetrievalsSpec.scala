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

package uk.gov.hmrc.core.retrieve.v2

import org.scalatest.OptionValues
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.auth.core.{ConfidenceLevel, MissingBearerToken}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ScpInformation}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.core.utils.{AuthUtils, BaseSpec}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws.writeableOf_JsValue
import play.api.test.Helpers.USER_AGENT

import java.util.UUID

class RetrievalsSpec extends BaseSpec with AuthUtils with OptionValues {

  "Credentials" should {
    "retrieve the correct data" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)
      val creds: Option[Credentials] = authorised(ConfidenceLevel.L250).retrieve(Retrievals.credentials)(Future.successful).futureValue
      creds shouldBe Some(Credentials(credId, "GovernmentGateway"))
    }
    "retrieve CL250 as CL250 i.e. not downgraded to CL200" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)
      val cl: ConfidenceLevel = authorised().retrieve(Retrievals.confidenceLevel)(Future.successful).futureValue
      cl shouldBe ConfidenceLevel.L250
    }
  }

  "TrustedHelper" should {
    "retrieve the correct data" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)

      val request = Json.parse("{\"attorneyName\":\"attorneyName\",\"principalName\":\"principalName\",\"link\":{\"text\":\"returnLink\",\"url\":\"returnLinkUrl\"},\"accounts\":{\"paye\":\"AA000003C\"}}")
      val authoriseDelegationResult = withClient { ws => ws.url(authResource("/auth/authoriseDelegation")).withHttpHeaders("Authorization" -> hc.authorization.get.value).post(request).futureValue }

      authoriseDelegationResult.status shouldBe 201

      val trustedHelper: Option[TrustedHelper] = authorised().retrieve(Retrievals.trustedHelper)(Future.successful).futureValue

      trustedHelper shouldBe Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", principalNino = Some("AA000003C")))
    }

    "retrieve the correct data with principalNino undefined if it's None in delegation context" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)

      val request = Json.parse("{\"attorneyName\":\"attorneyName\",\"principalName\":\"principalName\",\"link\":{\"text\":\"returnLink\",\"url\":\"returnLinkUrl\"},\"accounts\":{}}")
      val authoriseDelegationResult = withClient { ws => ws.url(authResource("/auth/authoriseDelegation")).withHttpHeaders("Authorization" -> hc.authorization.get.value).post(request).futureValue }

      authoriseDelegationResult.status shouldBe 201

      val trustedHelper: Option[TrustedHelper] = authorised().retrieve(Retrievals.trustedHelper)(Future.successful).futureValue

      trustedHelper shouldBe Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", principalNino = None))
    }

    "retrieve agent data correctly" in {
      val agentId = s"agentId-${UUID.randomUUID().toString}"
      val agentCode = s"agentCode-${UUID.randomUUID().toString}"
      val agentFriendlyName = s"agentFriendlyName-${UUID.randomUUID().toString}"

      implicit val headerCarrier = signWithAgentInfo(agentId, agentCode, agentFriendlyName)

      val agentInformation = authorised().retrieve(Retrievals.agentInformation)(Future.successful).futureValue
      agentInformation.agentId.value shouldBe agentId
      agentInformation.agentCode.value shouldBe agentCode
      agentInformation.agentFriendlyName.value shouldBe agentFriendlyName
    }
  }

  "scpInformation" should {
    val extraFields = Map(
      "scpSessionId" -> JsString("c88b769e-23e2-429c-aafc-9eba02e466f0"),
      "trustId" -> JsString("e7de76f4-8acb-49e5-b351-27f625b86f2b"),
      "trustIdChangedAt" -> JsString("2024-01-01T12:00:00.000Z"),
      "trustIdChangedBy" -> JsString("someUser"))

    "retrieve individual information correctly" in {
      val agentId = s"agentId-${UUID.randomUUID().toString}"
      val agentCode = s"agentCode-${UUID.randomUUID().toString}"
      val agentFriendlyName = s"agentFriendlyName-${UUID.randomUUID().toString}"

      implicit val headerCarrier = signWithAgentInfo(agentId, agentCode, agentFriendlyName, extraFields = extraFields)
        .withExtraHeaders(USER_AGENT -> "identity-provider-gateway") // these retrievals are allowlisted restricted to only select services.

      authorised().retrieve(Retrievals.scpSessionId)(Future.successful).futureValue shouldBe Some("c88b769e-23e2-429c-aafc-9eba02e466f0")
      authorised().retrieve(Retrievals.trustId)(Future.successful).futureValue shouldBe Some("e7de76f4-8acb-49e5-b351-27f625b86f2b")
      authorised().retrieve(Retrievals.trustIdChangedAt)(Future.successful).futureValue shouldBe Some("2024-01-01T12:00:00.000Z")
      authorised().retrieve(Retrievals.trustIdChangedBy)(Future.successful).futureValue shouldBe Some("someUser")
    }

    "retrieve combined information correctly" in {
      val agentId = s"agentId-${UUID.randomUUID().toString}"
      val agentCode = s"agentCode-${UUID.randomUUID().toString}"
      val agentFriendlyName = s"agentFriendlyName-${UUID.randomUUID().toString}"

      implicit val headerCarrier = signWithAgentInfo(agentId, agentCode, agentFriendlyName, extraFields = extraFields)
        .withExtraHeaders(USER_AGENT -> "identity-provider-gateway") // these retrievals are allowlisted restricted to only select services.

      val scpInformation = authorised().retrieve(Retrievals.scpInformation)(Future.successful).futureValue
      scpInformation shouldBe ScpInformation(
        scpSessionId     = Some("c88b769e-23e2-429c-aafc-9eba02e466f0"),
        trustId          = Some("e7de76f4-8acb-49e5-b351-27f625b86f2b"),
        trustIdChangedAt = Some("2024-01-01T12:00:00.000Z"),
        trustIdChangedBy = Some("someUser"))
    }
  }

  "identityProviderType" should {
    "retrieve information correctly" in {
      val agentId = s"agentId-${UUID.randomUUID().toString}"
      val agentCode = s"agentCode-${UUID.randomUUID().toString}"
      val agentFriendlyName = s"agentFriendlyName-${UUID.randomUUID().toString}"

      implicit val headerCarrier = signWithAgentInfo(agentId, agentCode, agentFriendlyName)
        .withExtraHeaders(USER_AGENT -> "identity-provider-gateway") // these retrievals are allowlisted restricted to only select services.

      authorised().retrieve(Retrievals.identityProviderType)(Future.successful).futureValue shouldBe Some("GG")
    }
  }

  "Authorisation" should {
    "fail" when {
      "no token is provided" in {
        implicit val hc = HeaderCarrier()
        authorised()(Future.unit).failed.futureValue shouldBe an[MissingBearerToken]
      }
    }
  }

}
