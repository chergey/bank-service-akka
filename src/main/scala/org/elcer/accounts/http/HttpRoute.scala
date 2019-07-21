package org.elcer.accounts.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.elcer.accounts.core.account.AccountService
import org.elcer.accounts.http.routes.{AccountRoute, UserRoute}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.elcer.accounts.core.user.UserService

import scala.concurrent.ExecutionContext

class HttpRoute(
                 accountService: AccountService,
                 authService: UserService,
                 secretKey: String
)(implicit executionContext: ExecutionContext) {

  private val accountRoute = new AccountRoute(secretKey, authService, accountService)
  private val userRoute  = new UserRoute(authService)

  val route: Route =
    cors() {
      pathPrefix("v1") {
        accountRoute.route ~
        userRoute.route
      } ~
      pathPrefix("healthcheck") {
        get {
          complete("OK")
        }
      }
    }

}
