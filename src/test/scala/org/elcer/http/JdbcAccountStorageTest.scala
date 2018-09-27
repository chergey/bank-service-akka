package org.elcer.http

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

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
        } yield accounts.size shouldBe 2)
      }

    }

    "updateBalance" should {
      "handle concurrency" in new EmptyContext {

        val TIMES = 1000

        val testAcc1: Account = Account(1, 32000, Random.nextString(10))
        val testAcc2: Account = Account(2, 31000, Random.nextString(10))

        val startBalance = testAcc1.balance + testAcc2.balance

        awaitNoResult(for {
          _ <- accountStorage.saveAccount(testAcc2)
          _ <- accountStorage.saveAccount(testAcc1)
        } yield null)

        var task1: Runnable = () =>
          accountStorage.transferFunds(testAcc1, testAcc2, Math.abs(Random.nextInt(100)))

        var task2: Runnable = () =>
          accountStorage.transferFunds(testAcc2, testAcc1, Math.abs(Random.nextInt(100)))

        val pool: ExecutorService = Executors.newFixedThreadPool(2)

        var i = TIMES
        while (i > 0) {
          pool.execute(task1)
          pool.execute(task2)
          i -= 1
        }

        pool.awaitTermination(Int.MaxValue, TimeUnit.SECONDS)

        awaitForResult(for {
          resultAcc1 <- accountStorage.getAccount(1)
          resultAcc2 <- accountStorage.getAccount(2)
        } yield {
          val endBalance = resultAcc1.get.balance + resultAcc2.get.balance
          println(endBalance, startBalance)
          endBalance shouldBe startBalance

        })
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


  trait EmptyContext {
    val accountStorage: AccountStorage = accountStorageBuilder()

    def createTestAcc(id: Int) =
      Account(id, Random.nextFloat(), Random.nextString(10))

  }


  trait Context extends EmptyContext {

    val testAccountId = Math.abs(Random.nextInt())
    val testAccountId2 = Math.abs(Random.nextInt())
    val testAcc1: Account = createTestAcc(testAccountId)
    val testAcc2: Account = createTestAcc(testAccountId2)

  }

}