package org.elcer.accounts

import java.net.InetAddress

import org.elcer.accounts.core.db.{DatabaseConnector, DatabaseMigrationManager}

object ApacheDbStorage {
  private val dbHost = InetAddress.getLocalHost
  val dbPort = 25535
  val dbName = "database-name"
  val dbUser = "user"
  val dbPassword = "user"
  val jdbcUrl = s"jdbc:derby:memory:$dbName;create=true"

  val flywayService = new DatabaseMigrationManager(jdbcUrl, dbUser, dbPassword)

  flywayService.dropDatabase()
  flywayService.migrateDatabaseSchema()

  val databaseConnector = new DatabaseConnector(
    ApacheDbStorage.jdbcUrl,
    ApacheDbStorage.dbUser,
    ApacheDbStorage.dbPassword
  )
}
