/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.auth

import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scalatestplus.mockito.MockitoSugar

class MockingConnectorSpec extends AnyWordSpec with MockitoSugar {

  "Auth connector" should {
    "allow mocking of response" in {
      implicit val hc = mock[HeaderCarrier]
      val mockAuthConnector: AuthConnector = mock[AuthConnector]
      val retrievalResult: Future[~[Credentials, Enrolments]] = Future.successful(new ~(Credentials("gg", "cred-1234"), Enrolments(Set(Enrolment("enrolment-value")))))

      Mockito.when(mockAuthConnector.authorise[~[Credentials, Enrolments]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      mockAuthConnector.authorise(EmptyPredicate, Retrievals.credentials and Retrievals.authorisedEnrolments)

      Mockito.verify(mockAuthConnector).authorise(equalTo(EmptyPredicate),
                                                  equalTo(Retrievals.credentials and Retrievals.authorisedEnrolments))(any(), any())
    }
  }

}
