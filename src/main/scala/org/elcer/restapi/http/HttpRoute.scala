package org.elcer.restapi.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.elcer.restapi.core.accounts.AccountService
import org.elcer.restapi.http.routes.{AccountRoute, AuthRoute}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.elcer.restapi.core.auth.AuthService

import scala.concurrent.ExecutionContext

class HttpRoute(
                 accountService: AccountService,
                 authService: AuthService,
                 secretKey: String
)(implicit executionContext: ExecutionContext) {

  private val accountRouter = new AccountRoute(authService, accountService)
  private val authRouter  = new AuthRoute(authService)

  val route: Route =
    cors() {
      pathPrefix("v1") {
        accountRouter.route ~
        authRouter.route
      } ~
      pathPrefix("healthcheck") {
        get {
          complete("OK")
        }
      }
    }

}
