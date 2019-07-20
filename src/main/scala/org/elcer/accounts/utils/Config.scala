package org.elcer.accounts.utils

import pureconfig.loadConfig

case class Config(secretKey: String, http: HttpConfig, database: DatabaseConfig)

object Config {

  import pureconfig.generic.auto.exportReader

  def load(): Config =
    loadConfig[Config] match {
      case Right(config: Config) => config
      case Left(error) =>
        throw new RuntimeException("Cannot read config file, errors:\n" + error.toList.mkString("\n"))
    }
}

private[utils] case class HttpConfig(host: String, port: Int)

private[utils] case class DatabaseConfig(jdbcUrl: String, username: String, password: String)
