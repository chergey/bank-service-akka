package org.elcer.accounts.utils.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

class DatabaseConnector(jdbcUrl: String, dbUser: String, dbPassword: String) {

  private val hikariDataSource = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(jdbcUrl)
    hikariConfig.setUsername(dbUser)
    hikariConfig.setPassword(dbPassword)

    new HikariDataSource(hikariConfig)
  }

  val account = slick.jdbc.DerbyProfile

  import account.api._

  val db: account.backend.DatabaseDef = Database.forDataSource(hikariDataSource, None)
  db.createSession()

}
