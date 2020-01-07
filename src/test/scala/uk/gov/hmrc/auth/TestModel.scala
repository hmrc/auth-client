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

package uk.gov.hmrc.auth

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.auth.core.authorise.Predicate


case class Foo(value: String)

object Foo {
  implicit val reads = Json.reads[Foo]
}

case class Bar(value: String, number: Int)

object Bar {
  implicit val reads = Json.reads[Bar]
}

case class TestPredicate1(value: String) extends Predicate {
  override def toJson: JsValue = Json.obj("testPredicate1" -> value)
}

case class TestPredicate2(value: String) extends Predicate {
  override def toJson: JsValue = Json.obj("testPredicate2" -> value)
}
