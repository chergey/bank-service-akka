package org.elcer.http

import akka.japi.Option
import org.elcer.BaseServiceTest
import org.elcer.restapi.core.Account
import org.elcer.restapi.core.accounts.{AccountStorage, InMemoryAccountStorage, JdbcAccountStorage}
import org.elcer.utils.InMemoryPostgresStorage

import scala.util.Random

class JdbcAccountStorageTest extends AccountStorageSpec {
  override def accountStorageBuilder(): AccountStorage =
    new JdbcAccountStorage(InMemoryPostgresStorage.databaseConnector)
}

class InMemoryAccountStorageTest extends AccountStorageSpec {
  override def accountStorageBuilder(): AccountStorage =
    new InMemoryAccountStorage()
}

abstract class AccountStorageSpec extends BaseServiceTest {

  def accountStorageBuilder(): AccountStorage

  "UserAccountStorage" when {

    "getAccount" should {

      "return account by id" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          _ <- accountStorage.saveAccount(testAcc2)
          maybeProfile <- accountStorage.getAccount(testAccountId)
        } yield maybeProfile shouldBe Some(testAcc1))
      }

      "return None if account not found" in new Context {
        awaitForResult(for {
          maybeProfile <- accountStorage.getAccount(111)
        } yield maybeProfile shouldBe None)
      }

    }

    "getAccounts" should {
      "return all accounts from database" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          _ <- accountStorage.saveAccount(testAcc2)
          accounts <- accountStorage.getAccounts
        } yield accounts.nonEmpty shouldBe true)
      }

    }

    "updateBalance" should {
      "1" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          maybeAcc <- accountStorage.getAccount(testAccountId)
        } yield  maybeAcc shouldBe Some(testAcc1))
      }
    }
    "saveAccount" should {

      "save account to database" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          maybeAcc <- accountStorage.getAccount(testAccountId)
        } yield maybeAcc shouldBe Some(testAcc1))
      }

    }

  }


  trait Context {
    val accountStorage: AccountStorage = accountStorageBuilder()
    val testAccountId = Math.abs(Random.nextInt())
    val testAccountId2 = Math.abs(Random.nextInt())
    val testAcc1: Account = createTestAcc(testAccountId)
    val testAcc2: Account = createTestAcc(testAccountId2)

    def createTestAcc(id: Int) = Account(id, Random.nextFloat(), Random.nextString(10))
  }

}