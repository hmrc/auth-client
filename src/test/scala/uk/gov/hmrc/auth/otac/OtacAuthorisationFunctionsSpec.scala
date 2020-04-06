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

package uk.gov.hmrc.auth.otac

import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.auth.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class OtacAuthorisationFunctionsSpec extends UnitSpec with MockitoSugar {

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val authConnectorMock: OtacAuthConnector = mock[OtacAuthConnector]

  class StubOtacAuthorisationFunctions(result: OtacAuthorisationResult) extends OtacAuthorisationFunctions {
    when(authConnectorMock.authorise(equalTo("myService"), any[HeaderCarrier], any[Option[String]])).thenReturn(Future.successful(result))
    override def authConnector: OtacAuthConnector = authConnectorMock
  }

  "OtacAuthorisationFunctions" should {

    "execute code if user is authorised" in {
      val otacToken: Option[String] = None

      await(new StubOtacAuthorisationFunctions(Authorised).withVerifiedPasscode("myService", otacToken){
        Future.successful(true)
      }) shouldBe true
    }

    "fail if user is unauthorised" in {
      val otacToken: Option[String] = None

      await(new StubOtacAuthorisationFunctions(Unauthorised).withVerifiedPasscode("myService", otacToken){
        Future.successful(true)
      }.recover {
        case OtacFailureThrowable(result) => result
      }) shouldBe Unauthorised
    }

  }

}
