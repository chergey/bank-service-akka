package org.elcer.accounts.http

import akka.http.scaladsl.server.Route
import org.elcer.accounts.BaseServiceTest
import org.elcer.accounts.core.account.AccountService
import org.elcer.accounts.core.auth.AuthService


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
