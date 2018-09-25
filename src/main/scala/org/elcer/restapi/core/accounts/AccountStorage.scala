package org.elcer.restapi.core.accounts

import org.elcer.restapi.core.Account
import org.elcer.restapi.utils.db.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait AccountStorage {

  def getAccounts: Future[Seq[Account]]

  def getAccount(id: Int): Future[Option[Account]]

  def saveAccount(account: Account): Future[Account]

  def updateBalance(from: Account, to: Account, amount: Float): Unit

}

class JdbcAccountStorage(
                          val databaseConnector: DatabaseConnector
                        )(implicit executionContext: ExecutionContext)
  extends AccountTable
    with AccountStorage {

  import databaseConnector._
  import databaseConnector.account.api._

  def getAccounts: Future[Seq[Account]] = db.run(accounts.result)

  def getAccount(id: Int): Future[Option[Account]] = db.run(accounts.filter(_.id === id).result.headOption)

  def saveAccount(account: Account): Future[Account] =
    db.run(accounts.insertOrUpdate(account)).map(_ => account)

  def updateBalance(from: Account, to: Account, amount: Float): Unit = {
    val fromRows = accounts.filter(_.id === from.id).map(_.balance)
    val toRows = accounts.filter(_.id === to.id).map(_.balance)
    val actions = (for {
      balanceFrom <- fromRows.result.headOption
      updateActionOptionFrom = balanceFrom.map(b => fromRows.update(-amount))
      affected <- updateActionOptionFrom.getOrElse(DBIO.successful(0))

      balanceTo <- toRows.result.headOption
      updateActionOptionTo = balanceTo.map(b => toRows.update(amount))
      affected <- updateActionOptionTo .getOrElse(DBIO.successful(0))
    } yield affected).transactionally

    db.run(actions)

  }

}

class InMemoryAccountStorage extends AccountStorage {

  private var state: Seq[Account] = Nil

  override def getAccounts: Future[Seq[Account]] =
    Future.successful(state)

  override def getAccount(id: Int): Future[Option[Account]] =
    Future.successful(state.find(_.id == id))

  override def saveAccount(account: Account): Future[Account] =
    Future.successful {
      state = state.filterNot(_.id == account.id)
      state = state :+ account
      account
    }

  override   def updateBalance(from: Account, to: Account, amount: Float): Unit = ???
}
