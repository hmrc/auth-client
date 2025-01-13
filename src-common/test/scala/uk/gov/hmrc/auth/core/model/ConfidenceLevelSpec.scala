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

package uk.gov.hmrc.auth.core.model

import play.api.libs.json.Json
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.ConfidenceLevel._

class ConfidenceLevelSpec extends UnitSpec {

  "ConfidenceLevel" should {

    "be comparable" in {

      val cl50 = ConfidenceLevel.fromInt(50).get
      val cl200 = ConfidenceLevel.fromInt(200).get
      val cl250 = ConfidenceLevel.fromInt(250).get
      val cl500 = ConfidenceLevel.fromInt(500).get
      val cl600 = ConfidenceLevel.fromInt(600).get

      cl50 should be < cl200
      cl200 should be < cl250
      cl250 should be < cl500
      cl500 should be < cl600
    }

    "be serializable to Json" in {
      L500.toJson shouldBe Json.obj("confidenceLevel" -> 500)
      L600.toJson shouldBe Json.obj("confidenceLevel" -> 600)
    }

    "have a confidence level 250" in {
      val cl250 = ConfidenceLevel.fromInt(250)
      cl250.isSuccess shouldBe true
      cl250.get shouldBe L250
    }

    "have a confidence level 600" in {
      val cl600 = ConfidenceLevel.fromInt(600)
      cl600.isSuccess shouldBe true
      cl600.get shouldBe L600
    }

    "not accept invalid level values" in {
      the[NoSuchElementException] thrownBy {
        ConfidenceLevel.fromInt(350)
      } should have message "Illegal confidence level: 350"

    }
  }

}
