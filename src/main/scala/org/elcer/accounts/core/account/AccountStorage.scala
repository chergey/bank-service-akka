package org.elcer.accounts.core.account

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.{Lock, ReentrantLock, ReentrantReadWriteLock}

import org.elcer.accounts.core.Account
import org.elcer.accounts.core.db.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait AccountStorage {

  def getAccounts: Future[Seq[Account]]

  def getAccount(id: Int): Future[Option[Account]]

  def saveAccount(account: Account): Future[Account]

  def transferFunds(from: Account, to: Account, amount: BigDecimal): Unit
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



  def transferFunds(from: Account, to: Account, amount: BigDecimal): Unit = {
    val fromRows = accounts.filter(_.id === from.id).map(_.balance)
    val toRows = accounts.filter(_.id === to.id).map(_.balance)

    val actions = (for {
      balanceFrom <- fromRows.result.headOption
      updateActionOptionFrom = balanceFrom.map(_ => fromRows.update(-amount))
      _ <- updateActionOptionFrom.getOrElse(DBIO.successful(0))

      balanceTo <- toRows.result.headOption
      updateActionOptionTo = balanceTo.map(_ => toRows.update(amount))
      affected <- updateActionOptionTo.getOrElse(DBIO.successful(0))
    } yield affected).transactionally

    db.run(actions)

  }

}

class InMemoryAccountStorage extends AccountStorage {

  private var state: Seq[Account] = Nil
  private val locks = scala.collection.mutable.Map[Int, Semaphore]()

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

  override def transferFunds(from: Account, to: Account, amount: BigDecimal): Unit = {
    val fromLock = locks.getOrElseUpdate(from.id, new Semaphore(1))
    val toLock = locks.getOrElseUpdate(to.id, new Semaphore(1))

    if (from.id < to.id) {
      fromLock.acquire()
      toLock.acquire()
    } else {
      toLock.acquire()
      fromLock.acquire()
    }

    from.balance -= amount
    to.balance += amount

    if (from.id < to.id) {
      fromLock.release()
      toLock.release()
    } else {
      toLock.release()
      fromLock.release()
    }
  }
}
