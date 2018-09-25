package org.elcer.restapi.core.accounts

import org.elcer.restapi.core.{Account, AccountUpdate}
import org.elcer.restapi.utils.MonadTransformers._

import scala.concurrent.{ExecutionContext, Future}

class AccountService(
                      accountStorage: AccountStorage
                    )(implicit executionContext: ExecutionContext) {

  def getAccounts: Future[Seq[Account]] =
    accountStorage.getAccounts

  def getAccount(id: Int): Future[Option[Account]] =
    accountStorage.getAccount(id)

  def createAccount(account: Account): Future[Account] =
    accountStorage.saveAccount(account)

  def transfer(from: Account, to: Account, amount: Float): Unit = {
    accountStorage.updateBalance(from, to, amount)
  }

  def updateAccount(id: Int, accountUpdate: AccountUpdate): Future[Option[Account]] =
    accountStorage
      .getAccount(id)
      .mapT(accountUpdate.merge)
      .flatMapTOuter(accountStorage.saveAccount)

}
