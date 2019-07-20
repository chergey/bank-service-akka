package org.elcer.accounts.core.account

import org.elcer.accounts.core.Account
import org.elcer.accounts.core.db.DatabaseConnector

private[account] trait AccountTable {

  protected val databaseConnector: DatabaseConnector
  import databaseConnector.account.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def id        = column[Int]("id", O.PrimaryKey)
    def balance = column[BigDecimal]("balance")
    def name  = column[String]("name")

    def * = (id, balance, name) <>
      ((Account.apply _).tupled, Account.unapply)
  }

  protected val accounts = TableQuery[Accounts]

}
