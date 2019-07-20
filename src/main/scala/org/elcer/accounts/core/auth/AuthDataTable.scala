package org.elcer.accounts.core.auth

import org.elcer.accounts.core.AuthData
import org.elcer.accounts.utils.db.DatabaseConnector

private[auth] trait AuthDataTable {

  protected val databaseConnector: DatabaseConnector

  import databaseConnector.account.api._

  class AuthDataSchema(tag: Tag) extends Table[AuthData](tag, "auth") {
    def id = column[Int]("id", O.PrimaryKey)

    def username = column[String]("username")

    def email = column[String]("email")

    def password = column[String]("password")

    def * = (id, username, email, password) <> ((AuthData.apply _).tupled, AuthData.unapply)
  }

  protected val auth = TableQuery[AuthDataSchema]

}
