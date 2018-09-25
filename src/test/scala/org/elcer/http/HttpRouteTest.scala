package org.elcer.http

import akka.http.scaladsl.server.Route
import org.elcer.BaseServiceTest
import org.elcer.restapi.core.accounts.AccountService
import org.elcer.restapi.http.HttpRoute
import org.elcer.restapi.core.auth.AuthService

class HttpRouteTest extends BaseServiceTest {

  "HttpRoute" when {

    "GET /healthcheck" should {

      "return 200 OK" in new Context {
        Get("/healthcheck") ~> httpRoute ~> check {
          responseAs[String] shouldBe "OK"
          status.intValue() shouldBe 200
        }
      }

    }

  }

  trait Context {
    val secretKey = "secret"
    val accountService: AccountService = mock[AccountService]
    val authService: AuthService = mock[AuthService]

    val httpRoute: Route = new HttpRoute(accountService, authService, secretKey).route
  }

}
