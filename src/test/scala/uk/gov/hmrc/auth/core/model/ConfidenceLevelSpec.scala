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
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.ConfidenceLevel.L500

import scala.util.Success

class ConfidenceLevelSpec extends UnitSpec {

  "ConfidenceLevel" should {

    "be comparable" in {

      for {
        c0 <- ConfidenceLevel.fromInt(0)
        c50 <- ConfidenceLevel.fromInt(50)
        c200 <- ConfidenceLevel.fromInt(200)
        c300 <- ConfidenceLevel.fromInt(300)
        c500 <- ConfidenceLevel.fromInt(500)
      } yield
        List(c0 < c50, c50 < c200, c200 < c300, c300 < c500) shouldBe
          Success(List(true, true, true, true))
    }

    "be serializable to Json" in {
      L500.toJson shouldBe Json.obj("confidenceLevel" -> 500)
    }

    "not accept invalid level values" in {
      the [NoSuchElementException] thrownBy {
        ConfidenceLevel.fromInt(250)
      } should have message "Illegal confidence level: 250"

    }
  }

}
