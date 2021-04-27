/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.auth.core.{ConfidenceLevel, MissingBearerToken}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.core.utils.{AuthUtils, BaseSpec}

class RetrievalsSpec extends BaseSpec with AuthUtils {

  "Credentials" should {
    "retrieve the correct data" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)
      val creds: Option[Credentials] = awaitAuth(authorised(ConfidenceLevel.L250).retrieve(Retrievals.credentials))
      creds shouldBe Some(Credentials(credId, "GovernmentGateway"))
    }
    "retrieve CL250 as CL250 i.e. not downgraded to CL200" in {
      val credId = randomCredId
      implicit val hc = signInGGWithAuthLoginApi(credId)
      val cl: ConfidenceLevel = awaitAuth(authorised().retrieve(Retrievals.confidenceLevel))
      cl shouldBe ConfidenceLevel.L250
    }
  }

  "Authorisation" should {
    "fail" when {
      "no token is provided" in {
        implicit val hc = HeaderCarrier()
        assertThrows[MissingBearerToken] {
          awaitAuth(authorised())
        }
      }
    }
  }


}
