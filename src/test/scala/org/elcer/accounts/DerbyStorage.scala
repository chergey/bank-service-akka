package org.elcer.accounts

import org.elcer.accounts.core.db.{DatabaseConnector, DatabaseMigrationManager}

object DerbyStorage {
  val dbName = "database-name"
  val dbUser = "user"
  val dbPassword = "user"
  val jdbcUrl = s"jdbc:derby:memory:$dbName;create=true"

  val flywayService = new DatabaseMigrationManager(jdbcUrl, dbUser, dbPassword)

  flywayService.dropDatabase()
  flywayService.migrateDatabaseSchema()

  val databaseConnector = new DatabaseConnector(
    DerbyStorage.jdbcUrl,
    DerbyStorage.dbUser,
    DerbyStorage.dbPassword
  )
}
