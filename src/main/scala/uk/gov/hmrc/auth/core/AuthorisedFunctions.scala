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


import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait AuthorisedFunctions {

  def authConnector: AuthConnector

  def authorised(): AuthorisedFunction = new AuthorisedFunction(EmptyPredicate)

  def authorised(predicate: Predicate): AuthorisedFunction = new AuthorisedFunction(predicate)

  class AuthorisedFunction(predicate: Predicate) {
    def apply[A](body: => Future[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      authConnector.authorise(predicate, EmptyRetrieval).flatMap(_ => body)
    def retrieve[A](retrieval: Retrieval[A]) = new AuthorisedFunctionWithResult(predicate, retrieval)
  }

  class AuthorisedFunctionWithResult[A](predicate: Predicate, retrieval: Retrieval[A]) {
    def apply[B](body: A => Future[B])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[B] =
      authConnector.authorise(predicate, retrieval).flatMap(body)
  }

}
