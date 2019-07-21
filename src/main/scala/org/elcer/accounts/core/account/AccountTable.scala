package org.elcer.accounts.core.account

import org.elcer.accounts.core.db.DatabaseConnector
import org.elcer.accounts.core.Account
import org.elcer.accounts.core.user.UserTable;

private[account] trait AccountTable extends UserTable {

  protected val databaseConnector: DatabaseConnector

  import databaseConnector.account.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "account") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("id")

    def balance = column[BigDecimal]("balance")

    def name = column[String]("name")

    def * = (id, userId, balance, name) <> ((Account.apply _).tupled, Account.unapply)

    def user = foreignKey("user", userId, users)(_.id)

  }

  protected val accounts = TableQuery[Accounts]

}
