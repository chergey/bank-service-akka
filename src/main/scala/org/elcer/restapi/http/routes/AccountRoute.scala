package org.elcer.restapi.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.elcer.restapi.core.accounts.AccountService
import org.elcer.restapi.core.auth.AuthService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


class AccountRoute(authService: AuthService, accountService: AccountService)(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import StatusCodes._
  import authService._

  val route: Route = pathPrefix("account") {
    path("transfer") {
      pathEndOrSingleSlash {
        get {
          parameters('from.as[String], 'to.as[String], 'amount.as[Float]) {
            (from, to, amount) =>
              complete {
                if (amount <= 0) {
                  complete(HttpResponse(StatusCodes.InternalServerError, entity = "Amount can'be equal or less zero!"))
                }

                Await.ready(accountService.getAccount(from), Duration.Inf).onComplete { res => {
                  res match {
                    case scala.util.Failure(value) => complete(HttpResponse(InternalServerError, entity = "Error"))
                    case scala.util.Success(fs) => fs match {
                      case None => complete(HttpResponse(InternalServerError, entity = s"Account $from not found"))
                      case Some(f) =>
                        Await.ready(accountService.getAccount(to), Duration.Inf).onComplete { res => {
                          res match {
                            case scala.util.Failure(_) => complete(HttpResponse(InternalServerError, entity = "Error"))
                            case scala.util.Success(ts) => ts match {
                              case None => complete(HttpResponse(InternalServerError, entity = s"Account $to not found"))
                              case Some(t) => accountService.transfer(f, t, amount)
                            }
                          }
                        }
                        }
                    }
                  }
                }

                }
              }
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