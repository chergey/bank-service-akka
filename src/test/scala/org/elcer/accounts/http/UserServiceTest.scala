package org.elcer.accounts.http

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import org.elcer.accounts.BaseServiceTest
import org.elcer.accounts.core.user.UserService
import org.elcer.accounts.http.routes.UserRoute
import org.mockito.Mockito._

import scala.concurrent.Future

class UserServiceTest extends BaseServiceTest {

  "AuthRoute" when {

    "POST /auth/signIn" should {

      "return 200 and token if sign in successful" in new Context {
        when(authService.signIn("test", "test")).thenReturn(Future.successful(Some("token")))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"login": "test", "password": "test"}""")

        Post("/auth/signIn", requestEntity) ~> authRoute ~> check {
          responseAs[String] shouldBe "\"token\""
          status.intValue() shouldBe 200
        }
      }

      "return 400 if signIn unsuccessful" in new Context {
        when(authService.signIn("test", "test")).thenReturn(Future.successful(None))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"login": "test", "password": "test"}""")

        Post("/auth/signIn", requestEntity) ~> authRoute ~> check {
          status.intValue() shouldBe 400
        }
      }

    }

    "POST /auth/signUp" should {

      "return 201 and token" in new Context {
        when(authService.signUp("test", "test", "test")).thenReturn(Future.successful("token"))
        val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"username": "test", "email": "test", "password": "test"}""")

        Post("/auth/signUp", requestEntity) ~> authRoute ~> check {
          responseAs[String] shouldBe "\"token\""
          status.intValue() shouldBe 201
        }
      }

    }

  }

  trait Context {
    val authService: UserService = mock[UserService]
    val authRoute: Route = new UserRoute(authService).route
  }

}
