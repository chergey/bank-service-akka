package org.elcer.restapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.elcer.restapi.core.auth.JdbcAuthDataStorage
import org.elcer.restapi.core.accounts.{JdbcAccountStorage, AccountService}
import org.elcer.restapi.http.HttpRoute
import org.elcer.restapi.utils.Config
import org.elcer.restapi.utils.db.{DatabaseConnector, DatabaseMigrationManager}
import org.elcer.restapi.core.auth.{AuthService, JdbcAuthDataStorage}

import scala.concurrent.ExecutionContext

object Boot extends App {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executor: ExecutionContext      = actorSystem.dispatcher
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
    val authDataStorage    = new JdbcAuthDataStorage(databaseConnector)

    val usersService = new AccountService(userProfileStorage)
    val authService  = new AuthService(authDataStorage, config.secretKey)
    val httpRoute    = new HttpRoute(usersService, authService, config.secretKey)

    Http().bindAndHandle(httpRoute.route, config.http.host, config.http.port)
  }

  startApplication()

}
