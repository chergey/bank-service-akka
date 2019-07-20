package org.elcer.accounts.core.account

import org.elcer.accounts.core.{Account, AccountUpdate}
import org.elcer.accounts.utils.MonadTransformers._

import scala.concurrent.{ExecutionContext, Future}

class AccountService(
                      accountStorage: AccountStorage
                    )(implicit executionContext: ExecutionContext) {

  def getAccounts: Future[Seq[Account]] = accountStorage.getAccounts

  def getAccount(id: Int): Future[Option[Account]] = accountStorage.getAccount(id)

  def createAccount(account: Account): Future[Account] =
    accountStorage.saveAccount(account)

  def transferFunds(from: Account, to: Account, amount: BigDecimal): Unit =
    accountStorage.transferFunds(from, to, amount)

  def updateAccount(id: Int, accountUpdate: AccountUpdate): Future[Option[Account]] =
    accountStorage
      .getAccount(id)
      .mapT(accountUpdate.merge)
      .flatMapTOuter(accountStorage.saveAccount)

}
