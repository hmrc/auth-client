play-auth
=========
[![Build Status](https://travis-ci.org/hmrc/play-auth.svg)](https://travis-ci.org/hmrc/play-auth) [ ![Download](https://api.bintray.com/packages/hmrc/releases/play-auth/images/download.svg) ](https://bintray.com/hmrc/releases/play-auth/_latestVersion) [![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Library for supporting user authorisation on microservices.

## Installing
 
Include the following dependency in your SBT build
 
``` scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")
 
libraryDependencies += "uk.gov.hmrc" %% "play-auth" % "[INSERT-VERSION]"
```

## Usage

### Using the function wrapper
First, in any controller, service or connector where you want to protect any part of your logic, mix in the AuthorisedFunctions trait:
``` scala 
// AuthorisedFunctions mixin
trait MyController extends BaseController with AuthorisedFunctions {
  
}
```
The concrete implementation of this controller must then satisfy the dependencies of the AuthorisedFunctions trait:

``` scala 
// Mixin Dependencies
object MyController extends MyController {
  val connector = AuthConnector
}
```
The AuthConnector instance itself is then usually defined somewhere in your wiring setup:
``` scala 
// AuthConnector Wiring
object AuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}
 
object WSHttp extends WSHttp {
  override val hooks: Seq[HttpHook] = NoneRequired
}
```
Note that above uses the old wiring style based on global objects here - you can also satisfy the dependencies in a DI-style if you wish.

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
authorised(Enrolment("SOME-ENROLMENT") and AuthProvider(GovernmentGateway)) {
  
  // your protected logic
}
```
In this example we require that the user has a SOME-ENROLMENT enrolment and only accept users logged in via Government Gateway. There are many other predicates that can be freely combined with and and/or or.

If you also want load some data for the current authority, add a retrieval call after the authorisation logic:
``` scala 
// With Data Retrieval
import uk.gov.hmrc.auth.core.Retrievals._
  
authorised(Enrolment("SOME-ENROLMENT)).retrieve(externalId) { externalId =>
 
  // your protected logic
}
```
Here we specify that we want to retrieve the externalId of the current user, which is a stable identifier that can be used by external systems to key data for that user. Note that the function block now takes a parameter, which gives you the requested data in a type-safe manner. For all available retrieval options see the section on Retrievals below.
If you request more than one data item, they will be passed in a flexible wrapper that can be best accessed via a pattern match in the body:
``` scala 
// Multiple Data Retrievals
import uk.gov.hmrc.auth.core.Retrievals._
  
authorised(Enrolment("SOME-ENROLMENT")).retrieve(internalId and userDetailsUri) {
 
  case internalId ~ userDetailsUri => // your protected logic
}
```
---
### Using Play Configuration
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
object MyGlobal extends DefaultFrontendGlobal {
  
  def resolveError(rh: RequestHeader, ex: Throwable) = ex match {
 
    case ex: InsufficientEnrolments => // your custom recovery logic, usually redirects
 
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
  
  case ex: InsufficientEnrolments => // your custom recovery logic, usually redirects
}
```

### Authorisation Exceptions

Whenever the auth microservice returns a 401 response to the library, it will contain a `WWW-Authenticate` header with details about the failure which will then be translated to a custom exception. They are all a subtype of `AuthorisationException`. This is the complete list of possible exceptions:
- InsufficientEnrolments: One or more of the requested enrolments were not present in the authority
- InsufficientConfidenceLevel: The confidence level requested for one of the enrolments was too low
- UnsupportedAuthProvider: the provider for the current authority is not one of the providers your auth predicates listed as accepted
- UnsupportedAffinityGroup: the requested affinityGroup did not match the one in the authority or the user is not a GG user
- UnsupportedCredentialRole: the requested credentialRole did not match the one in the authority or the user is not a GG user
- NoActiveSession: The user does not have an active session. This is an abstract type with 4 concrete subtypes, but you would usually handle all 4 types in the same way (most often by redirecting to the login page). The 4 subtypes are: MissingBearerToken (the user was probably not logged in), BearerTokenExpired (the user was logged in, but the session is expired), and two types which should never occur as they would hint at an internal error: InvalidBearerToken and SessionRecordNotFound.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

