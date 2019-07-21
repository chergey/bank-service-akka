package org.elcer.accounts.http.routes


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.generic.auto._
import org.elcer.accounts.core.user.UserService
import org.elcer.accounts.utils.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class UserRoute(authService: UserService)(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import StatusCodes._
  import authService._

  val route: Route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[LoginPassword]) { loginPassword =>
            complete(signIn(loginPassword.login, loginPassword.password)
              .map {
                case Some(token) => OK -> token
                case None => Unauthorized -> "Invalid login or password!"
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
