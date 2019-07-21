package org.elcer.accounts.http.routes


import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.circe.generic.auto._
import org.elcer.accounts.core.Account
import org.elcer.accounts.core.account.AccountService
import org.elcer.accounts.core.user.UserService
import org.elcer.accounts.utils.FailFastCirceSupport

import scala.concurrent.{ExecutionContext, Future}

class AccountRoute(secretKey: String, authService: UserService, accountService: AccountService)(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import StatusCodes._
  import org.elcer.accounts.utils.SecurityDirectives._

  implicit val bigDecimal: Unmarshaller[String, BigDecimal] =
    Unmarshaller.strict[String, BigDecimal](BigDecimal.apply)

  val route: Route = pathPrefix("account") {
    path("transfer") {
      pathEndOrSingleSlash {
        get {
          parameters(Symbol("from").as[Int], Symbol("to").as[Int], Symbol("amount").as[BigDecimal]) {
            (from, to, amount) =>
              complete {
                if (amount <= 0)
                  HttpResponse(StatusCodes.BadRequest, entity = "Amount can'be equal or less zero!")
                else
                  process(from, to, amount)
              }
          }
        }
      }
    } ~ pathPrefix("me") {
      pathEndOrSingleSlash {
        authenticate(secretKey) { userId =>
          entity(as[Account]) { acc =>
            acc.userId = userId
            complete(accountService.createAccount(acc).map(_.id.asInstanceOf[String])
              .recoverWith {
                case e =>
                  Future.failed(e)
              }
            )
          }
        }
      }
    }
  }

  private def process(from: Int, to: Int, amount: BigDecimal): Future[HttpResponse] = {
    //    ( for {
    //      f <- OptionT(accountService.getAccount(from)) if f.balance < amount
    //      t <- OptionT(accountService.getAccount(to))
    //      r <- OptionT.liftF(accountService.transferFunds(f, t, amount))
    //    } yield  r )  match {
    //      case s: OptionT[Future, Unit] => {
    //        Future.successful(HttpResponse(OK, entity = "Transferred successfully"))
    //      }
    //
    //      //      ).map(_ => HttpResponse(OK, entity = "Transferred successfully"))
    //      //      .getOrElse(HttpResponse(OK, entity = "Error in transfer"))
    //    }

    accountService.getAccount(from)
      .flatMap(_.map(f1 => {

        val result =
          if (f1.balance < amount)
            Future.successful(HttpResponse(BadRequest, entity = "Not enough money"))
          else
            accountService.getAccount(to).flatMap(_.map(t1 => {
              accountService.transferFunds(f1, t1, amount)
                .map(_ => HttpResponse(OK, entity = "Transferred successfully"))
                .recover {
                  case e: Exception => HttpResponse(OK, entity = s"Error in  transfer $e")
                }
            }).getOrElse(Future.successful(HttpResponse(BadRequest, entity = s"Account $to not found"))))

        result
      }).getOrElse(Future.successful(HttpResponse(BadRequest, entity = s"Account $from not found"))))

  }
}