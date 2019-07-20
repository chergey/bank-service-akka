package org.elcer.accounts

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}

trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar {

  def awaitForResult[T](futureResult: Future[T]): T =
    Await.result(futureResult, 5 seconds)
}
