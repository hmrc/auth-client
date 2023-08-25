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

package uk.gov.hmrc.auth.core.retrieve

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.auth.core._

import java.time.Instant
import java.util.UUID

@annotation.nowarn("msg=deprecated")
class RetrievalJsonSpec extends AnyWordSpec with ScalaFutures {

  "The JSON reads for the internalId retrieval" should {

    "read a populated id" in {
      val json = Json.parse("""{ "internalId": "xyz" }""")

      Retrievals.internalId.reads.reads(json).get shouldBe Some("xyz")
    }

    "read a null property as None" in {
      val json = Json.parse("""{ "internalId": null }""")

      Retrievals.internalId.reads.reads(json).get shouldBe None
    }

    "read a missing property as None" in {
      val json = Json.parse("""{ "internalXX": "xyz" }""")

      Retrievals.internalId.reads.reads(json).get shouldBe None
    }
  }

  "The JSON reads for the externalId retrieval" should {

    "read a populated id" in {
      val json = Json.parse("""{ "externalId": "xyz" }""")

      Retrievals.externalId.reads.reads(json).get shouldBe Some("xyz")
    }

    "read a null property as None" in {
      val json = Json.parse("""{ "externalId": null }""")

      Retrievals.externalId.reads.reads(json).get shouldBe None
    }

    "read a missing property as None" in {
      val json = Json.parse("""{ "externalXX": "xyz" }""")

      Retrievals.externalId.reads.reads(json).get shouldBe None
    }
  }

  "The JSON reads for the authProviderId retrieval" should {

    "read a GG credId" in {
      val json = Json.parse("""{ "authProviderId": { "ggCredId": "xyz" }}""")

      Retrievals.authProviderId.reads.reads(json).get shouldBe GGCredId("xyz")
    }

    "read a PAC clientId" in {
      val json = Json.parse("""{ "authProviderId": { "paClientId": "xyz" }}""")

      Retrievals.authProviderId.reads.reads(json).get shouldBe PAClientId("xyz")
    }

    "read a OneTimeLogin" in {
      val json = Json.parse("""{ "authProviderId": { "oneTimeLogin": "" }}""")

      Retrievals.authProviderId.reads.reads(json).get shouldBe OneTimeLogin
    }

    "read a StandardApplication clientId" in {
      val json = Json.parse("""{ "authProviderId": { "clientId": "app-1" }}""")

      Retrievals.authProviderId.reads.reads(json).get shouldBe StandardApplication("app-1")
    }

    "read a StandardApplication authProviderId using old auth format (backwards compatibility)" in {
      val json = Json.parse("""{ "authProviderId": { "applicationId": "app-1" }}""")

      Retrievals.authProviderId.reads.reads(json).get shouldBe StandardApplication("app-1")
    }

    "produce an error for unknown credential types" in {
      val json = Json.parse("""{ "authProviderId": { "fooBar": "xyz" }}""")

      Retrievals.authProviderId.reads.reads(json) shouldBe a[JsError]
    }
  }

  "The JSON reads for the userDetailsUri retrieval" should {

    "read a populated id" in {
      val json = Json.parse("""{ "userDetailsUri": "/user-details" }""")

      Retrievals.userDetailsUri.reads.reads(json).get shouldBe Some("/user-details")
    }

    "read a null property as None" in {
      val json = Json.parse("""{ "userDetailsUri": null }""")

      Retrievals.userDetailsUri.reads.reads(json).get shouldBe None
    }

    "read a missing property as None" in {
      val json = Json.parse("""{ "userDetailsXXX": "/user-details" }""")

      Retrievals.userDetailsUri.reads.reads(json).get shouldBe None
    }
  }

  "The JSON reads for the affinityGroup retrieval" should {

    "read an Individual affinity group" in {
      val json = Json.parse("""{ "affinityGroup": "Individual" }""")

      Retrievals.affinityGroup.reads.reads(json).get shouldBe Some(AffinityGroup.Individual)
    }

    "read an Organisation affinity group" in {
      val json = Json.parse("""{ "affinityGroup": "Organisation" }""")

      Retrievals.affinityGroup.reads.reads(json).get shouldBe Some(AffinityGroup.Organisation)
    }

    "read an Agent affinity group" in {
      val json = Json.parse("""{ "affinityGroup": "Agent" }""")

      Retrievals.affinityGroup.reads.reads(json).get shouldBe Some(AffinityGroup.Agent)
    }

    "produce an error for unknown credential types" in {
      val json = Json.parse("""{ "affinityGroup": "Bartender" }""")

      Retrievals.affinityGroup.reads.reads(json) shouldBe a[JsError]
    }

  }

  "The JSON reads for the loginTimes retrieval" should {

    val currentLogin = Instant.parse("2015-01-01T12:00:00.000Z")
    val previousLogin = Instant.parse("2012-01-01T12:00:00.000Z")

    "read login times with a previous login" in {
      val json = Json.parse("""{ "loginTimes": { "currentLogin": "2015-01-01T12:00:00.000Z", "previousLogin": "2012-01-01T12:00:00.000Z" }}""")

      Retrievals.loginTimes.reads.reads(json).get shouldBe LoginTimes(currentLogin, Some(previousLogin))
    }

    "read login times without a previous login" in {
      val json = Json.parse("""{ "loginTimes": { "currentLogin": "2015-01-01T12:00:00.000Z" }}""")

      Retrievals.loginTimes.reads.reads(json).get shouldBe LoginTimes(currentLogin, None)
    }

    "read login times without a previous login as null" in {
      val json = Json.parse("""{ "loginTimes": { "currentLogin": "2015-01-01T12:00:00.000Z", "previousLogin": null }}""")

      Retrievals.loginTimes.reads.reads(json).get shouldBe LoginTimes(currentLogin, None)
    }

  }

  "The JSON reads for the confidence level" should {
    "read confidence level value" in {
      val json = Json.parse("""{ "confidenceLevel": 200 }""")

      Retrievals.confidenceLevel.reads.reads(json).get shouldBe ConfidenceLevel.L200
    }
  }

  "The JSON reads for the enrolments retrieval" should {

    val enrolments = Set(
      Enrolment("ENROL-A", Seq(EnrolmentIdentifier("ID-A", "123")), "Activated"),
      Enrolment("ENROL-B", Seq(EnrolmentIdentifier("ID-B", "456")), "Activated")
    )

      def enrolmentsJson(retrieve: String) = Json.parse(
        s"""
         |{ "$retrieve": [
         |  {
         |    "key": "ENROL-A",
         |    "identifiers": [{"key":"ID-A","value":"123"}],
         |    "state": "Activated"
         |  },
         |  {
         |    "key": "ENROL-B",
         |    "identifiers": [{"key":"ID-B","value":"456"}],
         |    "state": "Activated"
         |  }
         |]}
         |""".stripMargin)

    "read all enrolments" in {
      val json = enrolmentsJson("allEnrolments")

      Retrievals.allEnrolments.reads.reads(json).get shouldBe Enrolments(enrolments)
    }

    "read authorised enrolments" in {
      val json = enrolmentsJson("authorisedEnrolments")

      Retrievals.authorisedEnrolments.reads.reads(json).get shouldBe Enrolments(enrolments)
    }
  }

  "The JSON reads for multiple retrievals" should {

    "read multiple result" in {

      val json = Json.parse(
        """
          |{
          |  "internalId": "123",
          |  "externalId": "456",
          |  "userDetailsUri": "/user-details"
          |}
        """.stripMargin)

      val retrieval = Retrievals.internalId and Retrievals.externalId and Retrievals.userDetailsUri

      retrieval.reads.reads(json).get match {
        case Some(internalId) ~ Some(externalId) ~ Some(userDetailsUri) =>
          internalId shouldBe "123"
          externalId shouldBe "456"
          userDetailsUri shouldBe "/user-details"
      }
    }

  }

  "The JSON reads for the OAuth Token retrieval" should {
    import v2.OauthTokens
    import v2.Retrievals.oauthTokens

    "read a fully populated OAuth Token object" in {
      val idToken = UUID.randomUUID().toString
      val accessToken = UUID.randomUUID().toString
      val refreshToken = UUID.randomUUID().toString

      val json = Json.obj(
        "oauthTokens" -> Json.obj(
          "idToken" -> idToken,
          "accessToken" -> accessToken,
          "refreshToken" -> refreshToken
        )
      )

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]

      tokens.get shouldBe Some(OauthTokens(Some(accessToken), Some(refreshToken), Some(idToken)))
    }

    "read an OAuth Token object with no ID Token" in {
      val accessToken = UUID.randomUUID().toString
      val refreshToken = UUID.randomUUID().toString

      val json = Json.obj(
        "oauthTokens" -> Json.obj(
          "accessToken" -> accessToken,
          "refreshToken" -> refreshToken
        )
      )

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]
      tokens.get shouldBe Some(OauthTokens(Some(accessToken), Some(refreshToken), None))
    }

    "read an OAuth Token object with no access token" in {
      val idToken = UUID.randomUUID().toString
      val refreshToken = UUID.randomUUID().toString

      val json = Json.obj(
        "oauthTokens" -> Json.obj(
          "idToken" -> idToken,
          "refreshToken" -> refreshToken
        )
      )

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]
      tokens.get shouldBe Some(OauthTokens(None, Some(refreshToken), Some(idToken)))
    }

    "read an OAuth Token object with no refresh token" in {
      val idToken = UUID.randomUUID().toString
      val accessToken = UUID.randomUUID().toString

      val json = Json.obj(
        "oauthTokens" -> Json.obj(
          "idToken" -> idToken,
          "accessToken" -> accessToken
        )
      )

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]
      tokens.get shouldBe Some(OauthTokens(Some(accessToken), None, Some(idToken)))
    }

    "read a null property as None" in {
      val json = Json.parse("""{"oauthTokens": null}""")

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]
      tokens.get shouldBe None
    }

    "read a missing property as None" in {
      val json = Json.obj()

      val tokens = oauthTokens.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]
      tokens.get shouldBe None
    }
  }

  "The JSON reads for profile, groupProfile, emailVerified" should {
    import v2.Retrievals.{emailVerified, groupProfile, profile}

    "read the values from the Json" in {
      val json = Json parse """{"profile": "someProfile", "groupProfile": "someGroupProfile", "emailVerified": true}"""

      profile.reads.reads(json).get shouldBe Some("someProfile")
      groupProfile.reads.reads(json).get shouldBe Some("someGroupProfile")
      emailVerified.reads.reads(json).get shouldBe Some(true)
    }

    "read missing values as None from the Json" in {
      val json = Json.obj()

      profile.reads.reads(json).get shouldBe None
      groupProfile.reads.reads(json).get shouldBe None
      emailVerified.reads.reads(json).get shouldBe None
    }

  }

  "The JSON reads for applicationName, applicationId and clientId" should {

    import v2.Retrievals.{applicationId, applicationName, clientId}

    "read the values from the Json" in {

      val json = Json.parse(
        """{"applicationName": "App 1",
          |"clientId": "client-1",
          |"applicationId": "app-1"}""".stripMargin)

      clientId.reads.reads(json).get shouldBe Some("client-1")
      applicationName.reads.reads(json).get shouldBe Some("App 1")
      applicationId.reads.reads(json).get shouldBe Some("app-1")

    }

    "read missing values as None from the Json" in {

      val json = Json.obj()

      clientId.reads.reads(json).get shouldBe None
      applicationName.reads.reads(json).get shouldBe None
      applicationId.reads.reads(json).get shouldBe None
    }

  }

  "The JSON reads for the trusted helper retrieval" should {
    import v2.Retrievals.trustedHelper
    import v2.TrustedHelper

    "read a fully populated trusted helper object" in {
      val principalName = UUID.randomUUID().toString
      val attorneyName = UUID.randomUUID().toString
      val returnLinkUrl = UUID.randomUUID().toString
      val principalNino = "AA000003D"

      val json = Json.obj(
        "trustedHelper" -> Json.obj(
          "principalName" -> principalName,
          "attorneyName" -> attorneyName,
          "returnLinkUrl" -> returnLinkUrl,
          "principalNino" -> Json.toJson(principalNino)
        )
      )

      val tokens = trustedHelper.reads.reads(json)
      tokens shouldBe a[JsSuccess[_]]

      tokens.get shouldBe Some(TrustedHelper(principalName, attorneyName, returnLinkUrl, principalNino))
    }

    "error when read a uncompleted trusted helper object e.g attorneyName is missing" in {
      val principalName = UUID.randomUUID().toString
      val returnLinkUrl = UUID.randomUUID().toString
      val principalNino = "AA000003D"

      val json = Json.obj(
        "trustedHelper" -> Json.obj(
          "principalName" -> principalName,
          "returnLinkUrl" -> returnLinkUrl,
          "principalNino" -> Json.toJson(principalNino)
        )
      )

      trustedHelper.reads.reads(json).isError shouldBe true
      trustedHelper.reads.reads(json).toString.contains("""JsError(List((/trustedHelper/attorneyName""") shouldBe true

    }
  }

}
