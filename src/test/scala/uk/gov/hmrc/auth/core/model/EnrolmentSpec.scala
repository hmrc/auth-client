/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class EnrolmentSpec extends UnitSpec {

  val enrolment = Enrolment(
    "foo",
    List(
      EnrolmentIdentifier("key-1", "val-1"),
      EnrolmentIdentifier("key-2", "val-2")),
    "activated",
    None)

  val enrolment2 = Enrolment(
    "bar",
    List(
      EnrolmentIdentifier("key-a", "val-a"),
      EnrolmentIdentifier("key-b", "val-b")),
    "activated",
    None)

  "Enrolments" should {

    "allow fetching a single enrolment by key" in {
      val setE = Enrolments(Set(enrolment, enrolment2))
      setE.getEnrolment("bar") shouldBe Some(enrolment2)
    }

  }

  "Enrolment" should {

    "allow fetching identifiers" in {
      enrolment.getIdentifier("key-1") shouldBe Some(EnrolmentIdentifier("key-1", "val-1"))
    }

    "allow identifiers to be added" in {
      val expanded = enrolment.copy(
        identifiers = enrolment.identifiers :+ EnrolmentIdentifier("foo", "bar"))
      enrolment.withIdentifier("foo", "bar") shouldBe expanded
    }

    "provide an isActivated property" in {
      enrolment.isActivated shouldBe true
    }

    "allow delegated auth rule to be added" in {
      val expanded = enrolment.copy(delegatedAuthRule = Some("delegated"))
      enrolment.withDelegatedAuthRule("delegated") shouldBe expanded
    }

    "be serializable as Json" in {
      enrolment.toJson shouldBe
        Json.obj(
          "enrolment" -> "foo",
          "identifiers" -> Json.arr(
            Json.obj(
              "key" -> "key-1",
              "value" -> "val-1"
            ),
            Json.obj(
              "key" -> "key-2",
              "value" -> "val-2"
            )
          ),
          "state" -> "activated"
        )
    }

  }

}
