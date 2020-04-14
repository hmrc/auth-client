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

package uk.gov.hmrc.auth.core.models

import play.api.libs.json.{Format, Json}

import scala.util.{Failure, Success, Try}

final case class ConfidenceLevel (confidenceLevel: Int) extends AnyVal

object ConfidenceLevel {

  implicit val ordering:Ordering[ConfidenceLevel] = Ordering.by[ConfidenceLevel,Int](_.confidenceLevel)

  val L500  = ConfidenceLevel(500)
  val L300  = ConfidenceLevel(300)
  val L200  = ConfidenceLevel(200)
  val L100  = ConfidenceLevel(100)
  val L50   = ConfidenceLevel(50)
  val L0    = ConfidenceLevel(50)

  implicit val format: Format[ConfidenceLevel] = Json.format[ConfidenceLevel]

  def fromInt(level: Int): Try[ConfidenceLevel] = level match {
    case 500 => Success(L500)
    case 300 => Success(L300)
    case 200 => Success(L200)
    case 100 => Success(L100)
    case 50  => Success(L50)
    case 0   => Success(L0)
    case _   => Failure(new IllegalArgumentException(s"Invalid confidence level: $level"))
  }
}