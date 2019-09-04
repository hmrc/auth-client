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

package uk.gov.hmrc.auth.delegation

import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait Delegator {

  protected def delegationConnector: DelegationAuthConnector

  val delegationStateSessionKey = "delegationState"

  def startDelegationAndRedirect(delegationContext: DelegationContext, redirectUrl: String)(implicit hc: HeaderCarrier, request: RequestHeader): Future[Result] = {
    delegationConnector.setDelegation(delegationContext).map { _ =>
      Results.SeeOther(redirectUrl).addingToSession(delegationStateSessionKey -> "On")
    }
  }

  def endDelegation(result: Result)(implicit hc: HeaderCarrier, request: RequestHeader): Future[Result] = {
    delegationConnector.endDelegation.map { _ =>
      result.removingFromSession(delegationStateSessionKey)
    }
  }

}

case class TaxIdentifiers(paye: Option[Nino] = None)
object TaxIdentifiers {
  implicit val format: OFormat[TaxIdentifiers] = Json.format[TaxIdentifiers]
}

case class Link(url: String, text: Option[String])
object Link {
  implicit val format: OFormat[Link] = Json.format[Link]
}

case class DelegationContext(principalName: String, attorneyName: String, link: Link, accounts: TaxIdentifiers)
object DelegationContext {
  implicit val format: OFormat[DelegationContext] = Json.format[DelegationContext]
}
