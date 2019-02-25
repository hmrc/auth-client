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

package uk.gov.hmrc.auth.core

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core.authorise.{AlternatePredicate, CompositePredicate, EmptyPredicate, Predicate}

class PredicateSpec extends WordSpec with ScalaFutures {

  "EmptyPredicate" should {

    "convert to an empty JSON array" in {
      val result = EmptyPredicate.toJson
      result shouldBe JsArray()

    }
  }

  "CompositePredicate" should {

    "convert to a JSON array containing all supplied Predicates" in {
      val predicate1 = new Predicate {
        val toJson = Json.obj("testPredicate1" -> "someValue1")
      }
      val predicate2 = new Predicate {
        val toJson = Json.obj("testPredicate2" -> "someValue2")
      }

      val expectedString =
        """[
          | {
          |   "testPredicate1": "someValue1"
          | },
          | {
          |   "testPredicate2": "someValue2"
          | }
          |]
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val result = CompositePredicate(predicate1, predicate2)

      result.toJson shouldBe expectedJson


    }
  }

  "AlternatePredicate" should {

    "convert to a JSON object containing an array with all supplied Predicates encapsulated with $or expression" in {
      val predicate1 = new Predicate {
        val toJson = Json.obj("testPredicate1" -> "someValue1")
      }
      val predicate2 = new Predicate {
        val toJson = Json.obj("testPredicate2" -> "someValue2")
      }

      val expectedString =
        """{
          | "$or": [
          |   {
          |     "testPredicate1": "someValue1"
          |   },
          |   {
          |     "testPredicate2": "someValue2"
          |   }
          | ]
          |}
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val result = AlternatePredicate(predicate1, predicate2)

      result.toJson shouldBe expectedJson


    }
  }

  "Complex nesting of predicates" should {

    "be possible and return correct json" in {
      val firstAnd1 = new Predicate {
        val toJson = Json.obj("firstAnd1" -> "someValue1")
      }
      val firstAnd2 = new Predicate {
        val toJson = Json.obj("firstAnd2" -> "someValue2")
      }
      val firstAnd3 = new Predicate {
        val toJson = Json.obj("firstAnd3" -> "someValue3")
      }

      val loneOr = new Predicate {
        val toJson = Json.obj("loneOr" -> "someValue4")
      }

      val secondAndOr1 = new Predicate {
        val toJson = Json.obj("secondAndOr1" -> "someValue5")
      }
      val secondAndOr2 = new Predicate {
        val toJson = Json.obj("secondAndOr2" -> "someValue6")
      }

      val secondAnd1 = new Predicate {
        val toJson = Json.obj("secondAnd1" -> "someValue3")
      }

      val expectedString =
        """{
          |  "$or": [
          |    [
          |      {
          |        "firstAnd1": "someValue1"
          |      },
          |      {
          |        "firstAnd2": "someValue2"
          |      },
          |      {
          |        "firstAnd3": "someValue3"
          |      }
          |    ],
          |    {
          |      "loneOr": "someValue4"
          |    },
          |    [
          |      {
          |        "$or": [
          |          {
          |            "secondAndOr1": "someValue5"
          |          },
          |          {
          |            "secondAndOr2": "someValue6"
          |          }
          |        ]
          |      },
          |      {
          |        "secondAnd1": "someValue3"
          |      }
          |    ]
          |  ]
          |}
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val firstAnd = CompositePredicate(CompositePredicate(firstAnd1, firstAnd2), firstAnd3)
      val secondAnd = CompositePredicate(AlternatePredicate(secondAndOr1, secondAndOr2), secondAnd1)
      val result = AlternatePredicate(AlternatePredicate(firstAnd, loneOr), secondAnd)

      result.toJson shouldBe expectedJson


    }
  }

}
