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

package uk.gov.hmrc.auth.core.model

import play.api.libs.json.Json
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.CredentialStrength
import uk.gov.hmrc.auth.core.CredentialStrength._

class CredentialStrengthSpec extends UnitSpec {

  "CredentialStrength" should {

    "contain weak and strong values" in {
      weak shouldBe "weak"
      strong shouldBe "strong"
    }

    "serialize to Json" in {
      CredentialStrength(weak).toJson shouldBe
        Json.obj("credentialStrength" -> "weak")
    }

  }

}
