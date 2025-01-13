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

package uk.gov.hmrc.core.utils

import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import play.api.test.WsTestClient
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames}

import java.util.UUID
import scala.util.Random

trait AuthUtils extends AuthorisedFunctions with WsTestClient {

  this: BaseSpec =>

  val random = new Random
  def randomNino = s"AA${900000 + random.nextInt(99999)}D"
  def randomCredId = s"credId-${random.nextInt(10101010)}"
  def randomGroupId = UUID.randomUUID().toString

  def authLoginApiResource(resource: String): String = s"http://localhost:8585$resource"

  def authResource(resource: String): String = s"http://localhost:8500$resource"

  def signInGGWithAuthLoginApi(credId: String = randomCredId, enrolments: Set[Enrolment] = Set.empty, nino: Option[String] = None, groupId: String = randomGroupId, confidenceLevel: Int = 250): HeaderCarrier = {
    implicit val idFormat: OFormat[EnrolmentIdentifier] = Json.format[EnrolmentIdentifier]
    implicit val enrolmentsFormat: OFormat[Enrolment] = Json.format[Enrolment]

    val request = Json.obj(
      "credId" -> credId,
      "groupIdentifier" -> groupId,
      "affinityGroup" -> "Individual",
      "confidenceLevel" -> confidenceLevel,
      "credentialStrength" -> "strong",
      "enrolments" -> Json.toJson(enrolments)) ++ nino.map(n => Json.obj("nino" -> nino)).getOrElse(Json.obj())
    val exchangeResult = withClient { ws => ws.url(authLoginApiResource("/government-gateway/session/login")).post(request).futureValue }

    exchangeResult.status shouldBe 201

    HeaderCarrier(
      authorization = exchangeResult.header(HeaderNames.authorisation).map(Authorization.apply))

  }

  def signWithAgentInfo(agentId: String, agentCode: String, agentFriendlyName: String, extraFields: Map[String, JsValue] = Map.empty): HeaderCarrier = {

    val request = Json.obj(
      "credId" -> randomCredId,
      "nino" -> randomNino,
      "groupIdentifier" -> randomGroupId,
      "usersName" -> "An Agent",
      "email" -> "someemail@email.com",
      "affinityGroup" -> "Individual",
      "confidenceLevel" -> 200,
      "credentialStrength" -> "strong",
      "credentialRole" -> "Admin",
      "agentId" -> agentId,
      "agentCode" -> agentCode,
      "agentFriendlyName" -> agentFriendlyName,
      "enrolments" -> Json.toJson(Seq.empty[Enrolment])) ++ JsObject(extraFields)
    val exchangeResult = withClient { ws => ws.url(authLoginApiResource("/government-gateway/session/login")).post(request).futureValue }

    exchangeResult.status shouldBe 201

    HeaderCarrier(
      authorization = exchangeResult.header(HeaderNames.authorisation).map(Authorization.apply))
  }

  def createSession(extraFields: Map[String, JsValue] = Map.empty): HeaderCarrier = {

    val request = Json.obj(
      "credId" -> randomCredId,
      "nino" -> randomNino,
      "confidenceLevel" -> 200,
      "scpSessionId" -> "c88b769e-23e2-429c-aafc-9eba02e466f0",
      "trustId" -> "e7de76f4-8acb-49e5-b351-27f625b86f2b",
      "trustIdChangedAt" -> "2024-01-01T12:00:00.000Z",
      "trustIdChangedBy" -> "hmrc",
      "enrolments" -> Json.toJson(Seq.empty[Enrolment]),
      "affinityGroup" -> "Individual",
      "sso" -> false,
      "twoFactorAuthEnabled" -> false) ++ JsObject(extraFields)
    val exchangeResult = withClient { ws => ws.url(authResource("/auth/sessions")).post(request).futureValue }

    exchangeResult.status shouldBe 201

    HeaderCarrier(
      authorization = exchangeResult.header(HeaderNames.authorisation).map(Authorization.apply))
  }

}
