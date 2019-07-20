package org.elcer.accounts.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.circe.generic.auto._
import org.elcer.accounts.core.Account
import org.elcer.accounts.core.account.AccountService
import org.elcer.accounts.core.auth.AuthService
import org.elcer.accounts.utils.FailFastCirceSupport

import scala.concurrent.{ExecutionContext, Future}

class AccountRoute(secretKey: String, authService: AuthService, accountService: AccountService)(implicit executionContext: ExecutionContext)
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

  private def process(from: Int, to: Int, amount: BigDecimal) = {
    accountService.getAccount(from)
      .zipWith(accountService.getAccount(to)) { (f, t) =>

        f.map(f1=>  {
          if (f1.balance < amount)
            HttpResponse(BadRequest, entity = "Not enough money")

          t.map(t1=> {
            accountService.transferFunds(f1, t1, amount)
            HttpResponse(OK, entity = "Transferred successfully")
          }).getOrElse( HttpResponse(BadRequest, entity = s"Account $to not found") )

        }).getOrElse(  HttpResponse(BadRequest, entity = s"Account $from not found"))

      }


    //      .map2(f => {
    //        if (f.balance < amount)
    //          HttpResponse(BadRequest, entity = "Not enough money")
    //
    //        accountService.getAccount(to)
    //          .map2(t => {
    //            //  throw new Exception("aaaaaaaaaaa")
    //            accountService.transferFunds(f, t, amount)
    //            HttpResponse(OK, entity = "Transferred successfully")
    //          }, HttpResponse(BadRequest, entity = s"Account $to not found"))
    //
    //
    //        HttpResponse(BadRequest, entity = s"AAAAAAAAAAAAAAAAAAAAAAAA")
    //
    //      }, HttpResponse(BadRequest, entity = s"Account $from not found"))


  }
}