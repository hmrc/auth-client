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

package uk.gov.hmrc.auth.otac

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class OtacAuthConnectorSpec extends UnitSpec with MockFactory {

  val stubbedCoreGet = stub[CoreGet]
  val serviceName = "myService"
  val mySerivceUrl = s"http://localhost:9000/$serviceName"

  def makeConnector(): PlayOtacAuthConnector = new PlayOtacAuthConnector {
    override val serviceUrl: String = mySerivceUrl
    override def http: CoreGet = stubbedCoreGet
  }

  def configureHttpStub(response: Future[HttpResponse]): Unit = {
    (stubbedCoreGet.GET[HttpResponse](
      _: String, _: Seq[(String, String)], _: Seq[(String, String)])(
        _: HttpReads[HttpResponse], _: HeaderCarrier, _: ExecutionContext))
      .when(mySerivceUrl + s"/authorise/read/$serviceName", *, *, *, *, *)
      .returns(response)
    ()
  }

  "OtacAuthConnectorSpec" should {

    "fail to authorise when no otac token provided" in {

      val connector = makeConnector()

      connector.authorise(serviceName, HeaderCarrier(), None)
        .futureValue shouldBe NoOtacTokenInSession
    }

    "authorise successfully when token accepted" in {

      configureHttpStub(Future.successful(HttpResponse(200)))
      val connector = makeConnector()

      connector.authorise(serviceName, HeaderCarrier(), Some("otac-token"))
        .futureValue shouldBe Authorised

    }

    "return unauthorized when token accepted, but service returns 401" in {

      configureHttpStub(Future.successful(HttpResponse(401)))
      val connector = makeConnector()

      connector.authorise(serviceName, HeaderCarrier(), Some("otac-token"))
        .futureValue shouldBe Unauthorised

    }

    "return unexpected error when token accepted, but service returns 500" in {

      configureHttpStub(Future.successful(HttpResponse(500)))
      val connector = makeConnector()

      connector.authorise(serviceName, HeaderCarrier(), Some("otac-token"))
        .futureValue shouldBe UnexpectedError(500)

    }

  }

}
