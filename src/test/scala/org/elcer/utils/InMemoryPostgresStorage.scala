package org.elcer.utils

import de.flapdoodle.embed.process.runtime.Network._
import org.elcer.restapi.utils.db.{DatabaseConnector, DatabaseMigrationManager}
import ru.yandex.qatools.embed.postgresql.{PostgresExecutable, PostgresProcess, PostgresStarter}
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.{Credentials, Net, Storage, Timeout}
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig
import ru.yandex.qatools.embed.postgresql.distribution.Version

object InMemoryPostgresStorage {
  val dbHost: String = getLocalHost.getHostAddress
  val dbPort = 25535
  val dbName = "database-name"
  val dbUser = "user"
  val dbPassword = "password"
  val jdbcUrl = s"jdbc:postgresql://$dbHost:$dbPort/$dbName"

  val psqlConfig = new PostgresConfig(
    Version.V9_6_3, new Net(dbHost, dbPort),
    new Storage(dbName), new Timeout(),
    new Credentials(dbUser, dbPassword)
  )
  val psqlInstance: PostgresStarter[PostgresExecutable, PostgresProcess] = PostgresStarter.getDefaultInstance
  val flywayService = new DatabaseMigrationManager(jdbcUrl, dbUser, dbPassword)

  val process: PostgresProcess = psqlInstance.prepare(psqlConfig).start()
  flywayService.dropDatabase()
  flywayService.migrateDatabaseSchema()

  val databaseConnector = new DatabaseConnector(
    InMemoryPostgresStorage.jdbcUrl,
    InMemoryPostgresStorage.dbUser,
    InMemoryPostgresStorage.dbPassword
  )
}
