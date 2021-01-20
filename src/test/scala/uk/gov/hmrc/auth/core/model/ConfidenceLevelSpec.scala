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

package uk.gov.hmrc.auth.core.model

import play.api.libs.json.Json
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.ConfidenceLevel._

import scala.util.Success

class ConfidenceLevelSpec extends UnitSpec {

  "ConfidenceLevel" should {

    "be comparable" in {

      for {
        c0 <- ConfidenceLevel.fromInt(0)
        c50 <- ConfidenceLevel.fromInt(50)
        c200 <- ConfidenceLevel.fromInt(200)
        c250 <- ConfidenceLevel.fromInt(250)
        c300 <- ConfidenceLevel.fromInt(300)
        c500 <- ConfidenceLevel.fromInt(500)
      } yield List(c0 < c50, c50 < c200, c200 < c250, c250 < c300, c300 < c500) shouldBe
        Success(List(true, true, true, true, true))
    }

    "be serializable to Json" in {
      L500.toJson shouldBe Json.obj("confidenceLevel" -> 500)
    }

    "have a confidence level 250" in {
      val cl250 = ConfidenceLevel.fromInt(250)
      cl250.isSuccess shouldBe true
      cl250.get shouldBe L250
    }

    "not accept invalid level values" in {
      the[NoSuchElementException] thrownBy {
        ConfidenceLevel.fromInt(350)
      } should have message "Illegal confidence level: 350"

    }
  }

}
