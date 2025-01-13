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

package uk.gov.hmrc.auth.core

import com.github.tomakehurst.wiremock.client.WireMock.{verify => _, _}
import com.github.tomakehurst.wiremock.client.{ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders, RequestMethod}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.auth.clientversion.ClientVersion
import uk.gov.hmrc.auth.core.retrieve.{CompositeRetrieval, EmptyRetrieval, SimpleRetrieval, ~}
import uk.gov.hmrc.auth.{Bar, Foo, TestPredicate1}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.ListHasAsScala

object Status {
  val OK = 200
  val UNAUTHORIZED = 401
}

class AuthConnectorSpec
  extends AnyWordSpec
  with WireMockSupport
  with HttpClientV2Support
  with ScalaFutures
  with IntegrationPatience {

  private lazy val anHttpClientV2 = httpClientV2

  private trait Setup {
    val clientVersion = ClientVersion.toString

    def withResponse: ResponseDefinitionBuilder => ResponseDefinitionBuilder =
      (_: ResponseDefinitionBuilder)
        .withStatus(Status.OK)
        .withBody("null")

    lazy val authConnector = new PlayAuthConnector {
      override val httpClientV2: HttpClientV2 = anHttpClientV2

      override val serviceUrl: String = s"${wireMockUrl}/some-service"

      wireMockServer.stubFor(
        WireMock.post(urlEqualTo("/some-service/auth/authorise"))
          .willReturn(withResponse(aResponse()))
      )
    }

    def exceptionHeaders(value: String, enrolment: Option[String] = None): HttpHeaders =
      new HttpHeaders(
        new HttpHeader(AuthenticateHeaderParser.WWW_AUTHENTICATE, s"""MDTP detail="$value""""),
        new HttpHeader(AuthenticateHeaderParser.ENROLMENT, enrolment.getOrElse(""))
      )
  }

  private trait UnauthorisedSetup extends Setup {
    def headerMsg: String

    override def withResponse =
      (_: ResponseDefinitionBuilder)
        .withStatus(Status.UNAUTHORIZED)
        .withHeaders(exceptionHeaders(headerMsg))
  }

  private trait FailedEnrolmentSetup extends Setup {
    def headerMsg: String
    def enrolment: String

    override def withResponse =
      (_: ResponseDefinitionBuilder)
        .withStatus(Status.UNAUTHORIZED)
        .withHeaders(exceptionHeaders(headerMsg, Some(enrolment)))
  }

  "authorise" should {
    val fooRetrieval = SimpleRetrieval("fooProperty", Foo.reads)
    val barRetrieval = SimpleRetrieval("barProperty", Bar.reads)

    "return a successful future when a 200 is returned and no retrievals are supplied" in new Setup {
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.futureValue
    }

    "return the correctly typed object when a 200 is returned and a retrieval is supplied" in new Setup {
      override def withResponse =
        (_: ResponseDefinitionBuilder)
          .withStatus(Status.OK)
          .withBody(
            """{
              | "fooProperty": {
              |   "value": "someValue"
              |  }
              |}
            """.stripMargin
          )

      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), fooRetrieval)

      result.futureValue shouldBe Foo("someValue")
    }

    "return the multiple correctly typed object when a 200 is returned and multiple retrievals are supplied" in new Setup {
      override def withResponse =
        (_: ResponseDefinitionBuilder)
          .withStatus(Status.OK)
          .withBody(
            """{
              | "fooProperty": {
              |   "value": "someValue"
              |  },
              | "barProperty": {
              |   "value": "someOtherValue",
              |   "number": 123
              |  }
              |}
            """.stripMargin
          )

      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), CompositeRetrieval(fooRetrieval, barRetrieval))

      val theFoo ~ theBar = result.futureValue
      theFoo shouldBe Foo("someValue")
      theBar shouldBe Bar("someOtherValue", 123)
    }

    "throw InsufficientConfidenceLevel on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "InsufficientConfidenceLevel"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[InsufficientConfidenceLevel]
    }

    "throw InsufficientEnrolments on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "InsufficientEnrolments"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[InsufficientEnrolments]
    }

    "throw InsufficientEnrolments on failed authorisation with appropriate header and retain failed enrolment" in new FailedEnrolmentSetup {
      val headerMsg = "InsufficientEnrolments"
      val enrolment = "SA-UTR"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue match {
        case InsufficientEnrolments("SA-UTR") => //success
        case other                            => fail(s"Did not match InsufficientEnrolment: $other")
      }
    }

    "throw BearerTokenExpired on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "BearerTokenExpired"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[BearerTokenExpired]
    }

    "throw MissingBearerToken on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "MissingBearerToken"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[MissingBearerToken]
    }

    "throw InvalidBearerToken on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "InvalidBearerToken"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[InvalidBearerToken]
    }

    "throw SessionRecordNotFound on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "SessionRecordNotFound"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[SessionRecordNotFound]
    }

    "throw FailedRelationship on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "FailedRelationship"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[FailedRelationship]
    }

    "throw IncorrectNino on failed authorisation with appropriate header" in new UnauthorisedSetup {
      val headerMsg = "IncorrectNino"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe IncorrectNino
    }

    "throw InternalError on failed authorisation with unknown header message" in new UnauthorisedSetup {
      val headerMsg = "some-unknown-header-message"
      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      whenReady(result.failed){ e =>
        e shouldBe a[InternalError]
        val internalError = e.asInstanceOf[InternalError]
        internalError.getMessage should include(headerMsg)
      }
    }

    "throw InternalError on failed authorisation with invalid header" in new UnauthorisedSetup {
      val headerMsg = "some-invalid-header-value"

      override def exceptionHeaders(value: String, enrolment: Option[String]) =
        new HttpHeaders(new HttpHeader(AuthenticateHeaderParser.WWW_AUTHENTICATE, headerMsg))

      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      whenReady(result.failed){ e =>
        e shouldBe a[InternalError]
        val internalError = e.asInstanceOf[InternalError]
        internalError.getMessage should include("InvalidResponseHeader")
      }
    }

    "throw InternalError on failed authorisation with missing header" in new Setup {
      override def withResponse =
        (_: ResponseDefinitionBuilder).withStatus(Status.UNAUTHORIZED)

      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      whenReady(result.failed){ e =>
        e shouldBe a[InternalError]
        val internalError = e.asInstanceOf[InternalError]
        internalError.getMessage should include("MissingResponseHeader")
      }
    }

    "throw MissingBearerToken when bearer token is missing in header" in new Setup {
      implicit lazy val hc: HeaderCarrier = HeaderCarrier()
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.failed.futureValue shouldBe a[MissingBearerToken]
    }

    // This test is to check the bugfix GG-8308 where an 8.x.x client version was incorrectly being reported as 15.x.x.
    // The test should be removed by the time the real auth-client-version actually reaches 15.
    "[GG-8308] not use an incorrect client version in the Auth-Client-Version header" in new Setup {
      def checkAuthClientVersionIsBelow15(authClientVersion: String): Unit = {
        val majorVersionRegex = """auth-client-([0-9]+)\..*""".r
        authClientVersion match {
          case majorVersionRegex(majorVersion) => majorVersion.toInt should be < 15
          case _                               => fail(s"client version header absent or in unexpected format: $clientVersionHeader")
        }
      }

      // The simple check in the line below should be sufficient...
      checkAuthClientVersionIsBelow15(ClientVersion.toString)
      // ...however to be safer we now also check the actual headers sent in the request:

      implicit lazy val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val result = authConnector.authorise(TestPredicate1("aValue"), EmptyRetrieval)

      result.futureValue

      val authoriseRequests: Seq[LoggedRequest] = wireMockServer.findRequestsMatching(RequestPatternBuilder.newRequestPattern(RequestMethod.POST, urlPathMatching(".*/auth/authorise")).build()).getRequests().asScala.toSeq
      val clientVersionHeader = authoriseRequests.head.getHeader("Auth-Client-Version")
      checkAuthClientVersionIsBelow15(clientVersionHeader)
    }
  }
}
