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

package uk.gov.hmrc.core.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.auth.core.{ AuthConnector, PlayAuthConnector }
import uk.gov.hmrc.http.CorePost
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.HttpClientV2Support

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

trait BaseSpec
  extends AnyWordSpecLike
  with GuiceOneAppPerSuite
  with Matchers
  with HttpClientV2Support {

  private lazy val anHttpClientV2 = httpClientV2

  val authConnector: AuthConnector =
    new PlayAuthConnector {
      override val serviceUrl: String = "http://localhost:8500"
      override val httpClientV2: HttpClientV2 = anHttpClientV2
    }

  implicit val defaultTimeout: FiniteDuration = 5.seconds
  implicit def extractAwait[A](future: Future[A]): A = await[A](future)

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
}
