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


class InsufficientConfidenceLevel extends AuthorisationException("Insufficient ConfidenceLevel")

class InsufficientEnrolments extends AuthorisationException("Insufficient Enrolments")

class UnsupportedAffinityGroup extends AuthorisationException("UnsupportedAffinityGroup")

class UnsupportedCredentialRole extends AuthorisationException("UnsupportedCredentialRole")

class UnsupportedAuthProvider extends AuthorisationException("UnsupportedAuthProvider")

class BearerTokenExpired extends NoActiveSession("Bearer token expired")

class MissingBearerToken extends NoActiveSession("Bearer token not supplied")

class InvalidBearerToken extends NoActiveSession("Invalid bearer token")

class SessionRecordNotFound extends NoActiveSession("Session record not found")

class InternalError(message: String) extends AuthorisationException(s"Internal Error, unexpected response: '$message'")

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
    case other => new InternalError(other)
  }

}
