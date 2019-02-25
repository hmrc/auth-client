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

package uk.gov.hmrc.auth.core

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.auth.core.retrieve.{CompositeRetrieval, EmptyRetrieval, SimpleRetrieval}
import uk.gov.hmrc.auth.{Bar, Foo}

class RetrievalSpec extends WordSpec with ScalaFutures {

  "EmptyRetrieval" should {

    "do nothing for any supplied JSON" in {
      val json = Json.parse(
        """
          |{
          | "some": "json",
          | "with": "data"
          |}
        """.stripMargin)

      val result = EmptyRetrieval.reads.reads(json)
      result shouldBe JsSuccess(())

    }
  }

  "SimpleRetrieval" should {

    "include only one property name" in {
      val result = SimpleRetrieval("someProperty", EmptyRetrieval.reads)

      result.propertyNames.nonEmpty shouldBe true
      result.propertyNames.size shouldBe 1
      result.propertyNames.head shouldBe "someProperty"

    }
  }

  "CompositeRetrieval" should {


    "contain all propertyNames and Reads from the supplied two SimpleRetrievals" in {
      val simpleRetrieval1 = SimpleRetrieval("fooProperty", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("barProperty", Bar.reads)
      val result = CompositeRetrieval(simpleRetrieval1, simpleRetrieval2)

      result.retrievalA shouldBe simpleRetrieval1
      result.retrievalB shouldBe simpleRetrieval2
      result.propertyNames should contain only("barProperty", "fooProperty")
    }

    "contain all propertyNames and Reads from the supplied SimpleRetrieval and CompositeRetrieval" in {
      val simpleRetrieval1 = SimpleRetrieval("fooProperty", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("bar1Property", Bar.reads)
      val simpleRetrieval3 = SimpleRetrieval("bar2Property", Bar.reads)
      val compositeRetrieval = CompositeRetrieval(simpleRetrieval2, simpleRetrieval3)
      val result = CompositeRetrieval(simpleRetrieval1, compositeRetrieval)

      result.retrievalA shouldBe simpleRetrieval1
      result.retrievalB shouldBe compositeRetrieval
      result.propertyNames should contain only("bar1Property", "fooProperty", "bar2Property")
    }

    "contain all propertyNames and Reads from the supplied CompositeRetrieval and SimpleRetrieval" in {
      val simpleRetrieval1 = SimpleRetrieval("fooProperty", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("bar1Property", Bar.reads)
      val simpleRetrieval3 = SimpleRetrieval("bar2Property", Bar.reads)
      val compositeRetrieval = CompositeRetrieval(simpleRetrieval2, simpleRetrieval3)
      val result = CompositeRetrieval(compositeRetrieval, simpleRetrieval1)

      result.retrievalA shouldBe compositeRetrieval
      result.retrievalB shouldBe simpleRetrieval1
      result.propertyNames should contain only("bar1Property", "fooProperty", "bar2Property")
    }

    "contain all propertyNames and Reads from the supplied two CompositeRetrievals" in {
      val simpleRetrieval1 = SimpleRetrieval("foo1Property", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("foo2Property", Foo.reads)
      val simpleRetrieval3 = SimpleRetrieval("bar1Property", Bar.reads)
      val simpleRetrieval4 = SimpleRetrieval("bar2Property", Bar.reads)
      val compositeRetrieval1 = CompositeRetrieval(simpleRetrieval1, simpleRetrieval2)
      val compositeRetrieval2 = CompositeRetrieval(simpleRetrieval3, simpleRetrieval4)
      val result = CompositeRetrieval(compositeRetrieval1, compositeRetrieval2)

      result.retrievalA shouldBe compositeRetrieval1
      result.retrievalB shouldBe compositeRetrieval2
      result.propertyNames should contain only("bar1Property", "foo1Property", "foo2Property", "bar2Property")
    }

    "contain only the propertyNames and Reads from the supplied SimpleRetrieval when an EmpyRetrieval is also supplied" in {
      val simpleRetrieval = SimpleRetrieval("fooProperty", Foo.reads)
      val result = CompositeRetrieval(simpleRetrieval, EmptyRetrieval)

      result.retrievalA shouldBe simpleRetrieval
      result.retrievalB shouldBe EmptyRetrieval
      result.propertyNames should contain only "fooProperty"
    }

    "contain only the propertyNames and Reads from the supplied CompositeRetrieval when an EmpyRetrieval is also supplied" in {
      val simpleRetrieval1 = SimpleRetrieval("fooProperty", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("barProperty", Bar.reads)
      val compositeRetrieval = CompositeRetrieval(simpleRetrieval1, simpleRetrieval2)
      val result = CompositeRetrieval(compositeRetrieval, EmptyRetrieval)

      result.retrievalA shouldBe compositeRetrieval
      result.retrievalB shouldBe EmptyRetrieval
      result.propertyNames should contain only("barProperty", "fooProperty")
    }

    "be able to handle nested CompositeRetrievals" in {
      val simpleRetrieval1 = SimpleRetrieval("foo1Property", Foo.reads)
      val simpleRetrieval2 = SimpleRetrieval("foo2Property", Foo.reads)
      val simpleRetrieval3 = SimpleRetrieval("bar1Property", Bar.reads)
      val simpleRetrieval4 = SimpleRetrieval("bar2Property", Bar.reads)
      val simpleRetrieval5 = SimpleRetrieval("foo3Property", Foo.reads)
      val simpleRetrieval6 = SimpleRetrieval("bar3Property", Bar.reads)
      val compositeRetrieval1 = CompositeRetrieval(simpleRetrieval1, simpleRetrieval2)
      val compositeRetrieval2 = CompositeRetrieval(simpleRetrieval3, simpleRetrieval4)
      val compositeRetrieval3 = CompositeRetrieval(compositeRetrieval1, compositeRetrieval2)
      val compositeRetrieval4 = CompositeRetrieval(simpleRetrieval5, simpleRetrieval6)


      val result = CompositeRetrieval(compositeRetrieval3, compositeRetrieval4)

      result.retrievalA shouldBe compositeRetrieval3
      result.retrievalB shouldBe compositeRetrieval4
      result.propertyNames should contain only("foo1Property", "foo2Property", "foo3Property", "bar1Property", "bar2Property", "bar3Property")
    }

  }

}
