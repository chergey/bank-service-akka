package org.elcer.accounts

import org.elcer.accounts.core.Account
import org.elcer.accounts.core.account.{AccountStorage, InMemoryAccountStorage, JdbcAccountStorage}
import org.elcer.accounts.core.db.{DatabaseConnector, DatabaseMigrationManager}
import org.elcer.accounts.utils.Config

import scala.concurrent.Future
import scala.util.Random

class JdbcAccountStorageTest extends AccountStorageSpec {
  override def accountStorageBuilder(): AccountStorage = {
    val config = Config.load()

    new DatabaseMigrationManager(config.database.jdbcUrl, config.database.username, config.database.password)
      .migrateDatabaseSchema()

    val databaseConnector = new DatabaseConnector(config.database.jdbcUrl, config.database.username,
      config.database.password)

    new JdbcAccountStorage(databaseConnector)
  }
}

class InMemoryAccountStorageTest extends AccountStorageSpec {
  override def accountStorageBuilder(): AccountStorage = new InMemoryAccountStorage()
}

abstract class AccountStorageSpec extends BaseServiceTest {

  def accountStorageBuilder(): AccountStorage

  "UserAccountStorage" when {

    "getAccount" should {

      "return account by id" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          maybeProfile <- accountStorage.getAccount(testAcc1.id)
        } yield maybeProfile shouldBe Some(testAcc1))
      }

      "return None if account not found" in new Context {
        awaitForResult(for {
          maybeAccount <- accountStorage.getAccount(1111111111)
        } yield maybeAccount shouldBe None)
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

        val TIMES = 10000

        val testAcc1: Account = Account(1, 1, 100000, Random.nextString(10))
        val testAcc2: Account = Account(2, 1, 100000, Random.nextString(10))

        private val startBalance = testAcc1.balance + testAcc2.balance
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc2)
          _ <- accountStorage.saveAccount(testAcc1)
        } yield null)


        var task1: () => Unit = () => transfer(testAcc1, testAcc2)
        var task2: () => Unit = () => transfer(testAcc2, testAcc1)

        runConcurrently(task1, task2)

        awaitForResult(for {
          resultAcc1 <- accountStorage.getAccount(1)
          resultAcc2 <- accountStorage.getAccount(2)
        } yield {
          val endBalance = resultAcc1.get.balance + resultAcc2.get.balance
          endBalance shouldBe startBalance
          println(endBalance, startBalance)
        })


        def transfer(debit: Account, credit: Account): Unit = {
          var i = TIMES
          while (i > 0) {
            val amount = Math.abs(Random.nextInt(1000))
            accountStorage.transferFunds(debit, credit, amount)
            i -= 1
          }
        }
      }
    }
    "saveAccount" should {

      "save account to database" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          maybeAcc <- accountStorage.getAccount(testAcc1.id)
        } yield maybeAcc shouldBe Some(testAcc1))
      }

    }


  }


  trait EmptyContext {
    val accountStorage: AccountStorage = accountStorageBuilder()

    def createTestAcc(id: Int) =
      Account(id, 1, BigDecimal(Random.nextInt()), Random.nextString(10))
  }


  def runConcurrently(tasks: () => Unit*): Unit = {
    if (tasks.isEmpty) throw new IllegalArgumentException("number of tasks must be > 0")

    val futures = collection.mutable.ListBuffer[Future[Unit]]()
    for (t <- tasks) {
      futures += Future[Unit] (t)
    }

    for (elem <- futures)
      awaitForResult(elem)

  }

  trait Context extends EmptyContext {
    private val testAccountId = Math.abs(Random.nextInt())
    private val testAccountId2 = Math.abs(Random.nextInt())
    protected val testAcc1: Account = createTestAcc(testAccountId)
    protected val testAcc2: Account = createTestAcc(testAccountId2)

  }

}