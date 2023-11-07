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

import java.util.UUID
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

trait AuthUtils extends AuthorisedFunctions {

  this: BaseSpec =>

  val random = new Random
  def randomNino = s"AA${900000 + random.nextInt(99999)}D"
  def randomCredId = s"credId-${random.nextInt(10101010)}"
  def randomGroupId = UUID.randomUUID().toString

  def awaitAuth[T](f: AuthorisedFunctionWithResult[T])(implicit hc: HeaderCarrier): T = await(f(Future.successful))
  def awaitAuth(f: AuthorisedFunction)(implicit hc: HeaderCarrier): Unit = await(f(Future.successful(())))

  def authLoginApiResource(resource: String): String = s"http://localhost:8585$resource"

  def signInGGWithAuthLoginApi(credId: String = randomCredId, enrolments: Set[Enrolment] = Set.empty, nino: Option[String] = None, groupId: String = randomGroupId): HeaderCarrier = {
    implicit val idFormat: OFormat[EnrolmentIdentifier] = Json.format[EnrolmentIdentifier]
    implicit val enrolmentsFormat: OFormat[Enrolment] = Json.format[Enrolment]

    val request = Json.obj(
      "credId" -> credId,
      "groupIdentifier" -> groupId,
      "affinityGroup" -> "Individual",
      "confidenceLevel" -> 250,
      "credentialStrength" -> "strong",
      "enrolments" -> Json.toJson(enrolments)
    ) ++ nino.map(n => Json.obj("nino" -> nino)).getOrElse(Json.obj())
    val exchangeResult = withClient { ws => await(ws.url(authLoginApiResource("/government-gateway/session/login")).post(request)) }

    exchangeResult.status shouldBe 201

    HeaderCarrier(
      authorization = exchangeResult.header(HeaderNames.authorisation) map Authorization
    )

  }

}
