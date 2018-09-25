package org.elcer.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import org.elcer.restapi.core.auth.AuthService

import scala.concurrent.ExecutionContext

class AuthRoute(authService: AuthService)(implicit executionContext: ExecutionContext) extends FailFastCirceSupport {

  import StatusCodes._
  import authService._

  val route: Route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[LoginPassword]) { loginPassword =>
            complete(
              signIn(loginPassword.login, loginPassword.password).map {
                case Some(token) => OK         -> token.asJson
                case None        => BadRequest -> None.asJson
              }
            )
          }
        }
      }
    } ~
    path("signUp") {
      pathEndOrSingleSlash {
        post {
          entity(as[UsernamePasswordEmail]) { userEntity =>
            complete(Created -> signUp(userEntity.username, userEntity.email, userEntity.password))
          }
        }
      }
    }
  }

  private case class LoginPassword(login: String, password: String)
  private case class UsernamePasswordEmail(username: String, email: String, password: String)

}
