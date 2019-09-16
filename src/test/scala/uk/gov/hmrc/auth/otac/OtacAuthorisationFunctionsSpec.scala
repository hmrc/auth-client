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

package uk.gov.hmrc.auth.otac

import java.nio.charset.Charset

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class OtacAuthorisationFunctionsSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite {

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  implicit val system = ActorSystem("test")
  implicit def mat: Materializer = ActorMaterializer()

  val TEST_VERIFICATION_FE = "localhost:9227"
  val TEST_REGIME = "myService"
  val authConnectorMock: OtacAuthConnector = mock[OtacAuthConnector]

  class StubOtacAuthorisationFunctions(result: OtacAuthorisationResult) extends OtacAuthorisationFunctions {
    when(authConnectorMock.authorise(equalTo(TEST_REGIME), any[HeaderCarrier], any[Option[String]])).thenReturn(Future.successful(result))
    override def authConnector: OtacAuthConnector = authConnectorMock

    override val useRelativeRedirect: Boolean = false

    override val serviceUrl : String = s"http://$TEST_VERIFICATION_FE"
  }

  "OtacAuthorisationFunctions" should {

    "execute code if user is authorised" in {
      val otacToken: Option[String] = None

      await(new StubOtacAuthorisationFunctions(Authorised).withVerifiedPasscode(TEST_REGIME, otacToken){
        Future.successful(true)
      }) shouldBe true
    }

    "fail if user is unauthorised" in {
      val otacToken: Option[String] = None

      await(new StubOtacAuthorisationFunctions(Unauthorised).withVerifiedPasscode(TEST_REGIME, otacToken){
        Future.successful(true)
      }.recover {
        case OtacFailureThrowable(result) => result
      }) shouldBe Unauthorised
    }

    "extract the passcode from the query string paramters" in {
      val req = FakeRequest.apply("GET", "http://localhost:8888/home?p=TEST_TOKEN")
      val functions = new StubOtacAuthorisationFunctions(Unauthorised)
      functions.tokenQueryParam(req) shouldBe "?p=TEST_TOKEN"
    }

    "create a redirect action that points to the verification frontend login page with the passcode and redirect in the session" in {
      implicit val req = FakeRequest.apply("GET", "/home?p=TEST_TOKEN")
        .withHeaders(HeaderNames.HOST -> "localhost:8888")
      val functions = new StubOtacAuthorisationFunctions(Unauthorised)
      val redirect = functions.redirectToLogin(req)
      redirect shouldBe a [Result]
      redirect.header.status shouldBe Status.SEE_OTHER
      redirect.header.headers.get(HeaderNames.LOCATION) shouldBe Some(s"http://$TEST_VERIFICATION_FE/verification/otac/login?p=TEST_TOKEN")
      redirect.session.get(SessionKeys.redirect) shouldBe Some("http://localhost:8888/home")
    }

    "create a redirect action that points to the verification frontend login page with the passcode and redirect in the session for Prod" in {
      implicit val req : RequestHeader = FakeRequest.apply("GET", "/home?p=TEST_TOKEN")
      val functions = new StubOtacAuthorisationFunctions(Unauthorised) {
        override val useRelativeRedirect: Boolean = true
      }
      val redirect = functions.redirectToLogin(req)
      redirect shouldBe a [Result]
      redirect.header.status shouldBe Status.SEE_OTHER
      redirect.header.headers.get(HeaderNames.LOCATION) shouldBe Some("/verification/otac/login?p=TEST_TOKEN")
      redirect.session.get(SessionKeys.redirect) shouldBe Some("/home")
    }

    "A request without a session OTAC token should attempt to login through the verification frontend" in {
      implicit val req : RequestHeader = FakeRequest.apply("GET", "/home?p=TEST_TOKEN")
        .withHeaders(HeaderNames.HOST -> "localhost:8888")
      val redirect = await(new StubOtacAuthorisationFunctions(Unauthorised).withPasscode(TEST_REGIME) {
        Future.successful(Results.Ok(""))
      })
      redirect shouldBe a [Result]
      redirect.header.status shouldBe Status.SEE_OTHER
      redirect.header.headers.get(HeaderNames.LOCATION) shouldBe Some(s"http://$TEST_VERIFICATION_FE/verification/otac/login?p=TEST_TOKEN")
      redirect.session.get(SessionKeys.redirect) shouldBe Some("http://localhost:8888/home")
    }

    "A request with a session OTAC token should verify that it is valid" in {
      implicit val req : RequestHeader = FakeRequest.apply("GET", "/home?p=TEST_TOKEN")
        .withHeaders(HeaderNames.HOST -> "localhost:8888").withSession(SessionKeys.otacToken -> "TEST_TOKEN")
      val redirect = await(new StubOtacAuthorisationFunctions(Authorised).withPasscode(TEST_REGIME) {
        Future.successful(Results.Ok("EXPECTED BODY"))
      })
      redirect shouldBe a [Result]
      redirect.header.status shouldBe Status.OK
      val bodyBytes: ByteString = await(redirect.body.consumeData)
      val body = bodyBytes.decodeString(Charset.defaultCharset().name)
      body shouldBe "EXPECTED BODY"
    }
  }

}
