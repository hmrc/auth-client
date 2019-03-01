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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}


class PathMatcherSpec extends WordSpec with ScalaFutures with Matchers {


  "the path matcher" should {

    "match a static path" in {
      PathMatcher("/foo/bar").matchPath("/foo/bar") shouldBe Some(Map())
    }

    "suffix match a static path" in {
      PathMatcher("/foo/bar").matchPath("/bla/foo/bar") shouldBe Some(Map())
    }

    "not match a static path" in {
      PathMatcher("/foo/bar").matchPath("/foo/baz") shouldBe None
    }

    "match exactly a static path" in {
      PathMatcher("/foo/bar").matchPath("/foo/bar/baz") shouldBe None
    }

    "match a path with one dynamic element" in {
      PathMatcher("/:var/bar").matchPath("/foo/bar") shouldBe Some(Map("var" -> "foo"))
    }

    "not match a path with one dynamic element" in {
      PathMatcher("/:var/bar").matchPath("/foo/baz") shouldBe None
    }

    "match a path with two dynamic elements" in {
      PathMatcher("/:var1/bar/:var2").matchPath("/foo/bar/bim") shouldBe Some(Map("var1" -> "foo", "var2" -> "bim"))
    }

    "not match a path with two dynamic elements" in {
      PathMatcher("/:var1/bar/:var2").matchPath("/foo/baz/bim") shouldBe None
    }

    "suffix match with dynamic elements" in {
      PathMatcher("/foo/:bar").matchPath("/bla/foo/ble") shouldBe Some(Map("bar" -> "ble"))
    }

    "match exactly with dynamic elements" in {
      PathMatcher("/foo/:bar").matchPath("/foo/ble/baz") shouldBe None
    }
  }
}
