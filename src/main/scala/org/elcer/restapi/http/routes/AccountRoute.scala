package org.elcer.restapi.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.elcer.restapi.core.Account
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
          parameters('from.as[Int], 'to.as[Int], 'amount.as[Float]) {
            (from, to, amount) =>
              complete {
                if (amount <= 0) {
                  HttpResponse(StatusCodes.InternalServerError, entity = "Amount can'be equal or less zero!")
                } else {
                  if (from > to)
                    process(from, to, amount)
                  else
                    process(to, from, amount)
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

  private def process(acc1: Int, acc2: Int, amount: Float): Unit = {
    Await.result(accountService.getAccount(acc1), Duration.Inf) match {
      case None => HttpResponse(InternalServerError, entity = s"Account $acc1 not found")
      case Some(a1) =>
        if (checkMoney(a1, amount)) {
          Await.result(accountService.getAccount(acc2), Duration.Inf) match {
            case None => HttpResponse(InternalServerError, entity = s"Account $acc2 not found")
            case Some(a2) =>
              accountService.transfer(a1, a2, amount)
              HttpResponse(StatusCodes.OK, entity = "Money transferred successfully!")
          }

        }
        else {
          HttpResponse(InternalServerError, entity = "Not enough money")
        }
    }

  }

  private def checkMoney(acc: Account, amount: Float): Boolean = {
    acc.balance >= amount
  }

  private case class LoginPassword(login: String, password: String)

  private case class UsernamePasswordEmail(username: String, email: String, password: String)

}