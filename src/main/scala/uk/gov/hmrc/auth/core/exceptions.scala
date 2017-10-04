/*
 * Copyright 2017 HM Revenue & Customs
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

abstract class AuthorisationException(val reason: String) extends RuntimeException(reason)

abstract class NoActiveSession(r: String) extends AuthorisationException(r)

case class InsufficientConfidenceLevel(msg: String = "Insufficient ConfidenceLevel") extends AuthorisationException(msg)

case class InsufficientEnrolments(msg: String = "Insufficient Enrolments") extends AuthorisationException(msg)

case class UnsupportedAffinityGroup(msg: String = "UnsupportedAffinityGroup") extends AuthorisationException(msg)

case class UnsupportedCredentialRole(msg: String = "UnsupportedCredentialRole") extends AuthorisationException(msg)

case class UnsupportedAuthProvider(msg: String = "UnsupportedAuthProvider") extends AuthorisationException(msg)

case class BearerTokenExpired(msg: String = "Bearer token expired") extends NoActiveSession(msg)

case class MissingBearerToken(msg: String = "Bearer token not supplied") extends NoActiveSession(msg)

case class InvalidBearerToken(msg: String = "Invalid bearer token") extends NoActiveSession(msg)

case class SessionRecordNotFound(msg: String = "Session record not found") extends NoActiveSession(msg)

case class InternalError(message: String = "Internal error") extends AuthorisationException(message)

object AuthorisationException {

  def fromString(reason: String): AuthorisationException = reason match {
    case "InsufficientConfidenceLevel" => new InsufficientConfidenceLevel
    case "InsufficientEnrolments" => new InsufficientEnrolments
    case "UnsupportedAffinityGroup" => new UnsupportedAffinityGroup
    case "UnsupportedCredentialRole" => new UnsupportedCredentialRole
    case "UnsupportedAuthProvider" => new UnsupportedAuthProvider
    case "BearerTokenExpired" => new BearerTokenExpired
    case "MissingBearerToken" => new MissingBearerToken
    case "InvalidBearerToken" => new InvalidBearerToken
    case "SessionRecordNotFound" => new SessionRecordNotFound
    case other => InternalError(other)
  }
}
