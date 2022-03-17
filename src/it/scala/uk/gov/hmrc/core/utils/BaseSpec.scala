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

package uk.gov.hmrc.core.utils

import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.WsTestClient
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.http.CorePost
import uk.gov.hmrc.http.test.HttpClientSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait BaseSpec
  extends WordSpecLike
     with WsTestClient
     with GuiceOneAppPerSuite
     with Matchers
     with HttpClientSupport {

  val authConnector: AuthConnector =
    new PlayAuthConnector {
      override val serviceUrl: String   = "http://localhost:8500"
      override val http      : CorePost = httpClient
    }

  implicit val defaultTimeout: FiniteDuration = 5.seconds
  implicit def extractAwait[A](future: Future[A]): A = await[A](future)

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

}
