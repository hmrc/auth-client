package uk.gov.hmrc.auth.otac

import play.api.mvc.Session
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait OtacAuthorisationResult

object Authorised extends OtacAuthorisationResult

object NoOtacTokenInSession extends OtacAuthorisationResult

object Unauthorised extends OtacAuthorisationResult

case class UnexpectedError(code: Int) extends OtacAuthorisationResult

case class OtacFailureThrowable(result: OtacAuthorisationResult) extends Throwable

trait OtacAuthConnector {
  def authorise(serviceName: String, headerCarrier: HeaderCarrier, session: Session): Future[OtacAuthorisationResult]
}

trait PlayOtacAuthConnector extends OtacAuthConnector {
  val serviceUrl: String

  def http: HttpGet

  def authorise(serviceName: String, headerCarrier: HeaderCarrier, session: Session): Future[OtacAuthorisationResult] =
    (session.get(SessionKeys.otacToken) match {
      case Some(otacToken) => {
        val enhancedHeaderCarrier =
          headerCarrier.withExtraHeaders(HeaderNames.otacAuthorization -> otacToken)
        callAuth(serviceName, enhancedHeaderCarrier).flatMap(toResult)
      }
      case None => Future.successful(NoOtacTokenInSession)
    })

  private def callAuth[A](serviceName: String, headerCarrier: HeaderCarrier): Future[Int] = {
    implicit val hc = headerCarrier
    http.GET(serviceUrl + s"/authorise/read/$serviceName").map(_.status)
  }

  private def toResult[T](status: Int): Future[OtacAuthorisationResult] =
    status match {
      case 200 => Future.successful(Authorised)
      case 401 => Future.successful(Unauthorised)
      case status => Future.successful(UnexpectedError(status))
    }
}