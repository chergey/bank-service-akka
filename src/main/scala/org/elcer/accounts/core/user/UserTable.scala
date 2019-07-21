package org.elcer.accounts.core.user

import org.elcer.accounts.core.User
import org.elcer.accounts.core.db.DatabaseConnector

private[core] trait UserTable {

  protected val databaseConnector: DatabaseConnector

  import databaseConnector.account.api._

  class UserSchema(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def username = column[String]("username")

    def email = column[String]("email")

    def password = column[String]("password")

    def * = (id, username, email, password) <> ((User.apply _)
      .tupled, User.unapply)
  }

  protected val users = TableQuery[UserSchema]

}
