package org.elcer.restapi.utils.db

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }

class DatabaseConnector(jdbcUrl: String, dbUser: String, dbPassword: String) {

  private val hikariDataSource = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(jdbcUrl)
    hikariConfig.setUsername(dbUser)
    hikariConfig.setPassword(dbPassword)

    new HikariDataSource(hikariConfig)
  }

  val account = slick.jdbc.PostgresProfile
  import account.api._

  val db = Database.forDataSource(hikariDataSource, None)
  db.createSession()

}
