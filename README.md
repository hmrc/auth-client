auth-client
=========
[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Library for supporting user authorisation on microservices.

## Change History

### v7.0 Nov 2023
Adds support for Play 2.9 and Play 3.0 and drops support for Play 2.6 and 2.7.
Versioning has changed to drop the play version - there will be an artefact for each play version supported.

### v6.2.0 Oct 2023
Update to README and code changes to remove service manager plugin.

### v6.0 Jan 2023
Auth Provider GDS/Verify is no longer supported. See 30th March 22 Tech Blog Post.

### v5.14.0 Jul 2022
Scala 2.13 - minor version increment.

### v5.13.0 May 2022
Removed dead OTAC & Passcode logic.

### v5.11.0 Feb 2022
Removed retrieval support for the deprecated unread_message_count attribute.

### v5.10.0 Feb 2022
Cross build for Scala 2.13.

### v5.9.0 Jan 2022
Check for presence/absence of the bearer token in the session cookie before calling auth to create a session.

Presence of the Authorization header will now be checked before making the call to auth to check the session, so tests mocking a successful authorised() call will now need to be updated to set this header as well

### v5.8.0 Jan 2022
Removed Joda time dependency.

Drop support for play 2.7 and play 2.6.

### v5.7.0 Jul 2021
Updated to latest version of sbt-auto-build and dropped Artifactory.

### v5.6.0 May 2021
Added full support for CL250, the Identity Verification (IV) Multiple Evidence Option (MEO).

For CLxxx meanings, see: https://confluence.tools.tax.service.gov.uk/display/VER/%27Confidence+Level%27+definitions

Earlier versions only supported CL50 or CL200, plus deprecated CL100.

See the May 2021 auth-client v5.x Tech Blog Post for further details inc. the full changelog under "Notes"

### v4.x Feb 2021, deprecated
Added support for Play 2.8, plus CL250 in part.

### v3.x deprecated

## Installing

Include the following dependency in your SBT build

``` scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

libraryDependencies += "uk.gov.hmrc" %% "auth-client-play-xx" % "[INSERT-VERSION]"
```

Where `play-xx` is your version of Play (e.g. `play-29`).

Note the version does not include the version of Play (as it did prior to version 7.0.0).

Note that this library is only available for Play 2.8.x and 2.9.x. No other play versions are supported.

> Play < 2.8 is no longer supported. If you plan to use it in a microservice that is still using an older version of Play, then you will need to upgrade it first.

## Usage

The overall approach is to add the library and take advantage of it by extending various classes. When a user first logs in they will not have an active session, and this must be handled by ensuring that your play global object extends uk.gov.hmrc.auth.frontend.Redirects and has overridden the 'resolveError' method that it provides. Your method should contain a NoActiveSession case and a default case as a bare minimum, see the Error handling / Redirects section below for more detail.

"auth-client is automatically included in your service, and is a transitive dependency provided by bootstrap-play which all microservices have within them. So there is no need to add auth-client as an explicit dependency. Additionally, keep the service's bootstrap-play library up to date so as to bring in the latest auth-client. Ideally, increment bootstrap-play to the latest version every time a microservice is updated. Same for the other library dependencies."

### Using the function wrapper
First, in any controller, service or connector where you want to protect any part of your logic, mix in the AuthorisedFunctions trait:
``` scala
// AuthorisedFunctions mixin
class MyController @Inject() (val authConnector: AuthConnector) extends BaseController with AuthorisedFunctions {

}
```

The AuthConnector instance itself is then usually defined somewhere in your wiring setup:
``` scala
// AuthConnector Wiring

class ConcreteAuthConnector(val serviceUrl: String,
                            val http: HttpPost) extends PlayAuthConnector
```

---

Depending on your requirements, you can define 0 to any number of predicates defining your authorisation logic, as well as 0 to any number of data retrievals from the current authority that you intend to use inside the function wrapper.
In the simplest case (no authorisation predicates, no data retrieval) you can use an empty wrapper like this:
``` scala
// Without Predicates
authorised() {
  // your protected logic
}
```
The minimal check that this code will still do is that the user is currently logged into the MDTP platform and has a bearer token in their session that is not yet expired. In case these conditions are not met, the function body will not execute.
If you do have a few authorisation requirements, you can pass them to the authorise function:
``` scala
// With Predicates
authorised(Relationship("TRUST",Set(BusinessKey("UTR","12345")))) {

  // your protected logic
}
```
In this example we require that the user has a relationship in the relationship establishment service, with the  relationship name of TRUST and the business key of a UTR of 12345.
``` scala
// With Predicates
authorised(Enrolment("SOME-ENROLMENT") and AuthProvider(GovernmentGateway)) {

  // your protected logic
}
```
In this example we require that the user has a SOME-ENROLMENT enrolment and only accept users logged in via Government Gateway.

There are many other predicates that can be freely combined with and and/or or.

If you also want load some data for the current authority, add a retrieval call after the authorisation logic:
``` scala
// With Data Retrieval
import uk.gov.hmrc.auth.core.retrieve.Retrievals._

authorised(Enrolment("SOME-ENROLMENT)).retrieve(externalId) { externalId =>

  // your protected logic
}
```
Here we specify that we want to retrieve the externalId of the current user, which is a stable identifier that can be used by external systems to key data for that user. Note that the function block now takes a parameter, which gives you the requested data in a type-safe manner. For all available retrieval options see the section on Retrievals below.
If you request more than one data item, they will be passed in a flexible wrapper that can be best accessed via a pattern match in the body:
``` scala
// Multiple Data Retrievals
import uk.gov.hmrc.auth.core.retrieve.Retrievals._

authorised(Enrolment("SOME-ENROLMENT")).retrieve(internalId and userDetailsUri) {

  case internalId ~ userDetailsUri => // your protected logic
}
```
Note the ~ for chaining the values in the pattern. While this syntax is unusual, it is the only cheap way of giving you any number of type-safe results inside the function without introducing a rather heavy-weight library like shapeless. You can combine any number of retrievals with and. A logical or is not available here as it would not make any sense for retrievals.
That's all there is to it. If you were one of the unlucky users who had to use one of our older auth libraries which forced you to implement an AuthProvider, a TaxRegime and maybe also a PageVisibilityPredicate and whatnot, I hope you appreciate the simplicity and consistency of the new library. We did not implement a single trait in the examples above, we merely used existing building blocks to express our requirements!

In order to check confidence level and retrieve its value, use the following snippet:
```scala
authorised(ConfidenceLevel.L200).retrieve(Retrievals.confidenceLevel) {
  case confidenceLevel => // logic
}
```

To check confidence level was obtained via multiple evidence sources, use CL250 instead as follows:
```scala
authorised(ConfidenceLevel.L250).retrieve(Retrievals.confidenceLevel) {
  case confidenceLevel => // logic
}
```

---
### Using Play Configuration - DEPRECATED

The config based approach should not be used for frontends.  A frontend will generally require more control over the handling of authorisation errors than is available in the filter chain to avoid raw http errors surfacing in the user''s browser.  Whilst this is not true of backend services which will generally just return any http error codes resulting from calls within the filter chain we believe that the wrapper function is a better approach being easier to understand, more explicit and more flexible.

The following shows an example for a configuration for two controllers:
``` YAML
controllers {

  authorisation {

    sa = {
      patterns = [
        "/my-service/enrolment1/:identifier"
        "/other-service/enrolment1/:identifier/"
      ]
      predicates = [{
        enrolment = "ENROLMENT1"
        identifiers = [{ key = "SOME-IDENTIFIER-KEY", value = "identifier" }]
      }]
    }

    ct = {
      patterns = [
        "/my-service/enrolment2/:identifier"
        "/other-service/enrolment2/:identifier/:rest"
      ]
      predicates = [{
        enrolment = "ENROLMENT2"
        identifiers = [{ key = "SOME-IDENTIFIER-KEY", value = "identifier" }]
      }]
    }

  }

  foo.FooController = {
    authorisedBy = ["enrolment1", "enrolment2"]
    needsLogging = false
    needsAuditing = false
  }

  bar.BarController = {
    authorisedBy = ["enrolment1"]
    needsLogging = false
    needsAuditing = false
  }

}
```
The example shows all aspects you need to define for protecting your endpoints:
#### 1. Add one or more authorisation sections
Inside the controllers section in the play configuration for your microservice, add an authorisation section, that contains one or more sections that define a concrete authorisation setup. In the example there are two sections named enrolment1 and enrolment2 respectively.
#### 2. Specify one or more URI patterns in each authorisation section
Within these sections, define one or more patterns for URIs that should be affected by this configuration. In the example both sections contain two path patterns each:
``` YAML
patterns = [
  "/my-service/enrolment1/:identifier"
  "/other-service/enrolment1/:identifier/"
]
```
Note that the pattern syntax is close to the one used for Play routes definition, it is not a regular expression like in our older library. Both paths above have several literal path elements like enrolment1, and one dynamic path element each (:identifier). These dynamic path elements match any string and the actual concrete value can be referred to in the predicate definitions as shown further below. For each incoming request the paths will be tried against the URI of the request in the order they are defined until one of them matches.
#### 3. Specify zero or more predicates in each authorisation section
Below the path patterns, define zero or any number of predicates that define the authorisation requirements for each incoming request:
``` YAML
predicates = [{
  enrolment = "ENROLMENT1"
  identifiers = [{ key = "SOME-IDENTIFIER-KEY", value = "identifier" }]
}]
```
In this example there is just one predicate. It specifies that the current user has to have an ENROLMENT-1 enrolment with a specific SOME-IDENTIFIER-KEY extracted from the URI. This ensures that all calls happen in this specific context. But as you see the predicates property is an array, so you can specify any number of predicates. They are combined with an implicit AND, if you need an OR combinator, you can use a mongo-style $or property:
``` YAML
predicates = [{
  $or = [
    { affinityGroup = "Organisation" }
    { affinityGroup = "Individual" }
  ]
}]
```
Here the endpoint is blocked for all agents, as we only allow either Organisations or Individuals in. Finally, like with the Scala API you can omit predicates altogether with an empty array:
``` YAML
predicates = []
```
In this case the filter will only check that the user is currently logged into the platform and has a bearer token in her session that is not yet expired.
For all the available predicates you can use see the Predicate Reference page.
#### 4. Link protected controllers to the authorisation sections you defined.
In our example one controller is linked to 2 authorisation presets:
``` YAML
foo.FooController = {
  authorisedBy = ["enrolment1", "enrolment2"]
  needsLogging = false
  needsAuditing = false
}
```
Here it will first try the 2 patterns defined in the "enrolment1" preset, and if any of the 2 matches, apply the predicates defined in there. If none of them matches, it tries the 2 patterns defined in the enrolment2 preset, and again, if any of the 2 matches, applies the predicates in that section. If no pattern matches it is considered an insecure call which will lead to a 401 response. This is to avoid that a new endpoint that got added to an existing controller with a pattern that does not match does not accidentally become unprotected. As a consequence this means that within a single controller all endpoints are either protected or not. If you need more flexibility, use the Scala API inside your controller implementations.
If you want to leave a controller unprotected, simply omit the authorisedBy predicate.

---
### Error handling
#### Backends
For backend services you usually do not want to explicitly handle unauthorised calls. It is usually the task of a frontend to determine whether a user is allowed to perform a certain action and then the frontend implementation ensures that all calls to backend services happen in an authorised context. If this is not the case, it is best to let the 401 response bubble back up to the frontend.
#### Frontends
In frontends you would usually want to avoid letting 401 responses bubble up to the user, so you have two options to recover from such a failure:

*Centralised Recovery in the Global object*

In many frontends, the recovery from certain types of authorisation failures will probably be similar. So keeping the logic in the Global object is convenient.
``` scala
import import play.api.mvc.Results._

object MyGlobal extends DefaultFrontendGlobal {

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = ex match {

    case ex: InsufficientEnrolments("HMRC-SA") => // your custom recovery logic, usually redirects

    case _ => super.resolveError(rh, ex)

  }
}
```

*Individual Recoveries in Controllers*

In rare cases where a single endpoint would require custom recovery logic, you can add it immediately after the authorised block in your controller logic:
``` scala
authorised(Enrolment("SOME-ENROLMENT")) {

  // your protected logic
} recoverWith {

  case ex: InsufficientEnrolments("SOME-ENROLMENT") => // your custom recovery logic, usually redirects
}
```

### Authorisation Exceptions

Whenever the auth microservice returns a 401 response to the library, it will contain a `WWW-Authenticate` header with details about the failure which will then be translated to a custom exception. They are all a subtype of `AuthorisationException`. This is the complete list of possible exceptions:
- InsufficientEnrolments(enrolmentName): One or more of the requested enrolments were not present in the authority, the first failing enrolment name is returned in the ```enrolment``` field
- InsufficientConfidenceLevel: The confidence level requested for one of the enrolments was too low
- UnsupportedAuthProvider: the provider for the current authority is not one of the providers your auth predicates listed as accepted
- UnsupportedAffinityGroup: the requested affinityGroup did not match the one in the authority or the user is not a GG user
- UnsupportedCredentialRole: the requested credentialRole did not match the one in the authority or the user is not a GG user
- NoActiveSession: The user does not have an active session. This is an abstract type with 4 concrete subtypes, but you would usually handle all 4 types in the same way (most often by redirecting to the login page). The 4 subtypes are: MissingBearerToken (the user was probably not logged in), BearerTokenExpired (the user was logged in, but the session is expired), and two types which should never occur as they would hint at an internal error: InvalidBearerToken and SessionRecordNotFound.
- FailedRelationship: The requested relationship in the authority did not match the one in the Relationship Establishment.

## Testing
In most cases testing a controller that uses auth-client is a matter of overriding the `AuthConnector` by providing a stubbed implementation.
Here you can find an example which retrieves credentials and enrolments:
```scala
  private class TestSetupAuthorisationDemoController(stubbedRetrievalResult: Future[_]) extends AuthorisationDemoController {
    override val authConnector: AuthConnector = new AuthConnector {

      def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
        stubbedRetrievalResult.map(_.asInstanceOf[A])
      }
    }
  }

  "calling retrieveProviderIdAndAuthorisedEnrolments" should {
    "return 200 for successful retrieval of 'authProviderId' and 'authorisedEnrolments' from auth-client" in {
      val retrievalResult: Future[~[LegacyCredentials, Enrolments]] = Future.successful(new ~(GGCredId("cred-1234"),
          Enrolments(Set(Enrolment("enrolment-value")))))
      val controller = new TestSetupAuthorisationDemoController(retrievalResult)

      val result = controller.retrieveProviderIdAndAuthorisedEnrolments()(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 401 for unsuccessful retrieval of 'authProviderId' and 'authorisedEnrolments' from auth-client" in {
      val controller = new TestSetupAuthorisationDemoController(Future.failed(InsufficientEnrolments()))

      val result = controller.retrieveProviderIdAndAuthorisedEnrolments()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }
  }

```

Another possibility is using `AuthConnector` as a class dependency. In that case you can rely on `AuthConnector#authorise()` being mocked.

```scala
    "allow mocking of response" in {
      implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val mockAuthConnector: AuthConnector = mock[AuthConnector]
      val retrievalResult: Future[~[Credentials, Enrolments]] = Future.successful(new ~(Credentials("gg", "cred-1234"),
          Enrolments(Set(Enrolment("enrolment-value")))))

      Mockito.when(mockAuthConnector.authorise[~[Credentials, Enrolments]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      mockAuthConnector.authorise(EmptyPredicate, Retrievals.credentials and Retrievals.authorisedEnrolments)

      Mockito.verify(mockAuthConnector).authorise(equalTo(EmptyPredicate),
        equalTo(Retrievals.credentials and Retrievals.authorisedEnrolments))(any(), any())
    }

```

When using Wiremock to test your controller's endpoint, you may get an error `Bearer token not supplied`.

To fix this, you can either provide an HTTP Authorization header
```scala
  wsClient.url("/baseUrl/endpoint")
    .withHttpHeaders("Authorization" -> "Bearer123")
    .get()
```
or provide a mocking session cookie as demonstrated below

```scala
  wsClient.url("/baseUrl/endpoint")
    .withCookies(mockSessionCookie)
    .get()

  // mocking session cookie
  def mockSessionCookie = {

    def makeSessionCookie(session:Session): Cookie = {
      val cookieCrypto = inject[SessionCookieCrypto]
      val cookieBaker = inject[SessionCookieBaker]
      val sessionCookie = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(Map(
      SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
      SessionKeys.authToken -> "mock-bearer-token",
      SessionKeys.sessionId -> "mock-sessionid"
    ))

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name
      override def value: String = cookie.value
      override def domain: Option[String] = cookie.domain
      override def path: Option[String] = Some(cookie.path)
      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)
      override def secure: Boolean = cookie.secure
      override def httpOnly: Boolean = cookie.httpOnly
    }
  }

```

For more testing examples head to `uk.gov.hmrc.auth.core.AuthConnectorSpec` or [Auth-Demo-Frontend]("https://github.com/hmrc/auth-demo-frontend")


## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
