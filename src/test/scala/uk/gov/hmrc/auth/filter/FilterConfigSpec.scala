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

package uk.gov.hmrc.auth.filter

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}


class FilterConfigSpec extends WordSpec with ScalaFutures with Matchers with ConfigSetup {


  val config = ConfigFactory.parseString(fullConfig).getConfig("controllers")
  val filterConfig = FilterConfig(config)


  "FilterConfig" should {

    "read valid configuration" in {

      val fooConfig = filterConfig.getConfigForController("foo.FooController")

      fooConfig should have size 2

      val saConfig = fooConfig.head
      saConfig.pathMatchers should have size 2
      saConfig.pathMatchers.head.matchPath("/foo/enrol1/123") shouldBe Some(Map("taxId" -> "123"))
      saConfig.pathMatchers.last.matchPath("/foo/enrol1/123/rest") shouldBe Some(Map("taxId" -> "123", "rest" -> "rest"))
      saConfig.predicatesAsJson shouldBe """[{"enrolment":"ENROL-1","identifiers":[{"key":"BOO","value":"$taxId"}]}]"""

      val ctConfig = fooConfig.last
      ctConfig.pathMatchers should have size 2
      ctConfig.pathMatchers.head.matchPath("/foo/enrol2/123") shouldBe Some(Map("taxId" -> "123"))
      ctConfig.pathMatchers.last.matchPath("/foo/enrol2/123/rest") shouldBe Some(Map("taxId" -> "123", "rest" -> "rest"))
      ctConfig.predicatesAsJson shouldBe """[{"enrolment":"ENROL-2","identifiers":[{"key":"AHH","value":"$taxId"}]}]"""

      val barConfig = filterConfig.getConfigForController("bar.BarController")

      barConfig should have size 1
      barConfig.head shouldBe fooConfig.head

    }

    "return an empty sequence for controllers without configuration" in {

      filterConfig.getConfigForController("duh.UnknownController") should have size 0

    }

    "return an empty sequence for controllers without a authorisedBy property" in {

      filterConfig.getConfigForController("baz.BazController") should have size 0

    }

    "throw an exception for controllers with an authorisedBy entry pointing to a non-existing configuration" in {

      a[RuntimeException] shouldBe thrownBy {
        filterConfig.getConfigForController("bim.BimController") should have size 0
      }

    }

  }


}
