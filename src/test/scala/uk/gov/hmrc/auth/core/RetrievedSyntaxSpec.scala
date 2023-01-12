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

package uk.gov.hmrc.auth.core

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.{GGCredId, LegacyCredentials, ~}

/**
 * Retrieved syntax is intended for use in unit tests in code that uses auth-client,
 * it makes it more convenient to construct values to return from fake or mock AuthConnectors
 */
class RetrievedSyntaxSpec extends WordSpec with Matchers {

  "syntax.retrieved._" should {

    "allow ~ instances to be constructed using infix and" in {
      import uk.gov.hmrc.auth.core.syntax.retrieved._
      val retrieved: String ~ String ~ String = "foo" and "bar" and "baz"

      retrieved shouldBe new ~(new ~("foo", "bar"), "baz")
    }

    "allow ~ instances constructed using infix and to be assigned to val of more general type" in {
      import uk.gov.hmrc.auth.core.syntax.retrieved._
      val retrieved: Option[String] ~ LegacyCredentials ~ Option[AffinityGroup] ~ ConfidenceLevel =
        None and GGCredId("some-cred-id") and Some(Individual) and ConfidenceLevel.L200

      retrieved shouldBe new ~(new ~(new ~(None, GGCredId("some-cred-id")), Some(Individual)), ConfidenceLevel.L200)
    }
  }
}
