package org.elcer.core.accounts

import java.util.UUID

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

    "getProfile" should {

      "return account by id" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          _ <- accountStorage.saveAccount(testAcc2)
          maybeProfile <- accountStorage.getAccount(testProfileId1)
        } yield maybeProfile shouldBe Some(testAcc1))
      }

      "return None if account not found" in new Context {
        awaitForResult(for {
          maybeProfile <- accountStorage.getAccount("smth")
        } yield maybeProfile shouldBe None)
      }

    }

    "getProfiles" should {

      "return all accounts from database" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          _ <- accountStorage.saveAccount(testAcc2)
          accounts <- accountStorage.getAccounts
        } yield accounts.nonEmpty shouldBe true)
      }

    }

    "saveAccount" should {

      "save account to database" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1)
          maybeAcc <- accountStorage.getAccount(testProfileId1)
        } yield maybeAcc shouldBe Some(testAcc1))
      }

      "overwrite balance if it exists" in new Context {
        awaitForResult(for {
          _ <- accountStorage.saveAccount(testAcc1.copy(name = "test", balance = 1000))
          _ <- accountStorage.saveAccount(testAcc1)
          maybeAcc <- accountStorage.getAccount(testProfileId1)
        } yield maybeAcc shouldBe Some(testAcc1))
      }

    }

  }

  trait Context {
    val accountStorage: AccountStorage = accountStorageBuilder()
    val testProfileId1: String = UUID.randomUUID().toString
    val testProfileId2: String = UUID.randomUUID().toString
    val testAcc1: Account = testProfile(testProfileId1)
    val testAcc2: Account = testProfile(testProfileId2)

    def testProfile(id: String) = Account(id, Random.nextFloat(), Random.nextString(10))
  }

}
