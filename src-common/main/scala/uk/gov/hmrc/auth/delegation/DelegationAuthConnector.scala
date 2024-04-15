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

import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

trait DelegationAuthConnector {
  implicit val legacyRawReads: HttpReads[HttpResponse] =
    HttpReads.Implicits.throwOnFailure(HttpReads.Implicits.readEitherOf(HttpReads.Implicits.readRaw))

  val authServiceUrl: String

  def httpClientV2: HttpClientV2

  def setDelegation(delegationContext: DelegationContext)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClientV2
      .post(url"$authServiceUrl/auth/authoriseDelegation")
      .withBody(Json.toJson(delegationContext))
      .execute[HttpResponse]

  def endDelegation()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClientV2
      .delete(url"$authServiceUrl/auth/endDelegation")
      .execute[HttpResponse]
}
