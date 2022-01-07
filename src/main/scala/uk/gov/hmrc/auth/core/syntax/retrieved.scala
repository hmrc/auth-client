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

package uk.gov.hmrc.auth.core.syntax

import uk.gov.hmrc.auth.core.retrieve.~

import scala.language.implicitConversions

/**
 * Convenience syntax for retrieved (~) - for usage examples see RetrievedSyntaxSpec
 */
trait RetrievedSyntax {
  implicit def authSyntaxForRetrieved[A](a: A): RetrievedOps[A] = new RetrievedOps[A](a)
}

final class RetrievedOps[A](val a: A) extends AnyVal {
  def and[B](b: B): A ~ B = new ~(a, b)
}
