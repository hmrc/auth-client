/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.play.json

import play.api.libs.json.{JsString, JsSuccess}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.{Foo, UnitSpec}

class MappingsSpec extends UnitSpec {

  "EnumMappings" should {

    "find an enum by string name" in {

      val mapped: EnumMapping[AffinityGroup] =
        Mappings.mapEnum[AffinityGroup](Individual, Organisation, Agent)

      mapped.fromString("Individual") should be (Some(Individual))

    }

  }

  "Mappings" should {

    "create a mapping between domains" in {

      val mapping: Mapping[String, Foo] = Mappings.map[String, Foo](Foo.apply, _.value)
      mapping should not be null
      mapping.jsonReads.reads(JsString("blah")) should be (JsSuccess(Foo("blah")))
      mapping.jsonWrites.writes(Foo("blah")) should be (JsString("blah"))
    }

    "create an optional mapping between domains" in {

      val mapping: Mapping[String, Foo] =
        Mappings.mapOption[String, Foo](x => Some(Foo(x)), _.value)
      mapping should not be null
      mapping.jsonReads.reads(JsString("blah")) should be (JsSuccess(Foo("blah")))
      mapping.jsonWrites.writes(Foo("blah")) should be (JsString("blah"))

    }

    "create an either mapping between domains" in {

      val mapping: Mapping[String, Foo] =
        Mappings.mapEither[String, Foo](x => Right(Foo(x)), _.value)
      mapping should not be null
      mapping.jsonReads should not be null
      mapping.jsonWrites should not be null
    }

  }

}
