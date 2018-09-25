package org.elcer.restapi.core.accounts

import org.elcer.restapi.core.Account
import org.elcer.restapi.utils.db.DatabaseConnector

private[accounts] trait AccountTable {

  protected val databaseConnector: DatabaseConnector
  import databaseConnector.account.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def id        = column[Int]("id", O.PrimaryKey)
    def balance = column[Float]("balance")
    def name  = column[String]("name")

    def * = (id, balance, name) <>
      ((Account.apply _).tupled, Account.unapply)
  }

  protected val accounts = TableQuery[Accounts]

}
