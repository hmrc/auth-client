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

package uk.gov.hmrc.auth.core

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core.authorise.{AffinityGroup, AndPred, AuthProviders, ConfidenceLevel, CredentialRole, CredentialStrength, EmptyPredicate, Enrolment, Nino, OrPred, Relationship}
import uk.gov.hmrc.auth.core.predicates._

class PredicateSpec extends WordSpec with ScalaFutures {

  "EmptyPredicate" should {

    "convert to an empty JSON array" in {
      val result = Json.toJson(EmptyPredicate())
      result shouldBe JsArray()

    }
  }

  "CompositePredicate" should {

    "convert to a JSON array containing all supplied Predicates" in {
      val predicate1 = Nino(true,None)
      val predicate2 = AffinityGroup(models.Individual)

      val expectedString =
        """[
          | {
          |  "hasNino" : true
          | },
          | {
          |   "affinityGroup": "Individual"
          | }
          |]
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val result = AndPred(List(predicate1, predicate2))

      Json.toJson(result) shouldBe expectedJson


    }
  }

  "AlternatePredicate" should {

    "convert to a JSON object containing an array with all supplied Predicates encapsulated with $or expression" in {
      val predicate1 = Nino(true,None)
      val predicate2 = AffinityGroup(models.Individual)

      val expectedString =
        """{
          | "$or": [
          |   {
          |      "hasNino" : true
          |   },
          |   {
          |     "affinityGroup": "Individual"
          |   }
          |  ]
          |}
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val result = OrPred(List(predicate1, predicate2))

      println(Json.toJson(result))

      Json.toJson(result) shouldBe expectedJson


    }
  }

  "Complex nesting of predicates" should {

    "be possible and return correct json" in {
      val firstAnd1 = Nino(hasNino = true , Some(models.Nino("AB000001")))

      val firstAnd2 = Enrolment(models.Enrolment("IR-SA"))

      val firstAnd3 = ConfidenceLevel(models.ConfidenceLevel.L200)

      val loneOr = CredentialStrength(models.Strong)

      val secondAndOr1 = AuthProviders(models.GovernmentGateway)
      val secondAndOr2 = AffinityGroup(models.Organisation)
      val secondAnd1 = CredentialRole(models.User)

      val expectedString =
        """{
          |  "$or": [
          |    [
          |      {
          |        "hasNino": true,
          |        "nino" : "AB000001"
          |      },
          |      {
          |        "enrolment": "IR-SA",
          |        "identifiers" : [],
          |        "state": "Activated"
          |      },
          |      {
          |        "confidenceLevel": 200
          |      }
          |    ],
          |    {
          |      "credentialStrength": "strong"
          |    },
          |    [
          |      {
          |        "$or": [
          |          {
          |            "authProviders": ["GovernmentGateway"]
          |          },
          |          {
          |            "affinityGroup": "Organisation"
          |          }
          |        ]
          |      },
          |      {
          |        "credentialRole": "User"
          |      }
          |    ]
          |  ]
          |}
        """.stripMargin

      val expectedJson = Json.parse(expectedString)

      val firstAnd = AndPred(firstAnd1, firstAnd2, firstAnd3)
      val secondAnd = AndPred(OrPred(secondAndOr1, secondAndOr2), secondAnd1)
      val result = OrPred(firstAnd, loneOr, secondAnd)

      Json.toJson(result) shouldBe expectedJson


    }
  }
  "Relationship Predicate" should {
    "be able to correctly convert a relationship Predicate into json" in {

      val relationshipName = "TRUST"

      val businessKeyUTR = models.BusinessKey("UTR","12345")
      val businessKeyPostcode = models.BusinessKey("PostCode","SW4 7HR")

      val relationshipPredicateUTR = Relationship(models.Relationship(relationshipName,Set(businessKeyUTR)))

      val relationshipPredicateUTRAndPostcode = authorise.Relationship(models.Relationship(relationshipName,Set(businessKeyUTR, businessKeyPostcode)))

      val relationshipJsonWithOneBusinessKey :String =
        s"""|{
            |    "relationshipName": "TRUST",
            |      "businessKeys": [
            |        {
            |          "name": "UTR",
            |          "value": "12345"
            |        }
            |      ]
            |}
    """.stripMargin

        val relationshipJsonWithTwoBusinessKeys :String =
        s"""|{
            |    "relationshipName": "TRUST",
            |      "businessKeys": [
            |        {
            |          "name": "UTR",
            |          "value": "12345"
            |        },
            |        {
            |          "name": "PostCode",
            |          "value": "SW4 7HR"
            |        }
            |      ]
            |}
    """.stripMargin

     Json.parse(relationshipJsonWithOneBusinessKey) shouldBe Json.toJson(relationshipPredicateUTR)
     Json.parse(relationshipJsonWithTwoBusinessKeys) shouldBe Json.toJson(relationshipPredicateUTRAndPostcode)


    }
  }

}
