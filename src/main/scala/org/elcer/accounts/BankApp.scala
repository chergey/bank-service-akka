package org.elcer.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.elcer.accounts.core.account.{AccountService, JdbcAccountStorage}
import org.elcer.accounts.core.auth.{AuthService, JdbcAuthDataStorage}
import org.elcer.accounts.http.HttpRoute
import org.elcer.accounts.utils.Config
import org.elcer.accounts.utils.db.{DatabaseConnector, DatabaseMigrationManager}

import scala.concurrent.ExecutionContext

object BankApp extends App {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config = Config.load()

    new DatabaseMigrationManager(
      config.database.jdbcUrl,
      config.database.username,
      config.database.password
    ).migrateDatabaseSchema()

    val databaseConnector = new DatabaseConnector(
      config.database.jdbcUrl,
      config.database.username,
      config.database.password
    )

    val userProfileStorage = new JdbcAccountStorage(databaseConnector)
    val authDataStorage = new JdbcAuthDataStorage(databaseConnector)

    val usersService = new AccountService(userProfileStorage)
    val authService = new AuthService(authDataStorage, config.secretKey)
    val httpRoute = new HttpRoute(usersService, authService, config.secretKey)

    Http().bindAndHandle(httpRoute.route, config.http.host, config.http.port)
  }

  startApplication()

}
