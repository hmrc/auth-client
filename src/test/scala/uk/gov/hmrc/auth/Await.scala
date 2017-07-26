package uk.gov.hmrc.auth

import scala.concurrent.{Await, Future}

trait Await {
  import scala.concurrent.duration._

  implicit val defaultTimeout = 5 seconds

  implicit def extractAwait[A](future: Future[A]) = await[A](future)

  def await[A](future: Future[A])(implicit timeout: Duration) = Await.result(future, timeout)
}
