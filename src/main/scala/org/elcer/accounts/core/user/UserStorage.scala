package org.elcer.accounts.core.user

import org.elcer.accounts.core.User
import org.elcer.accounts.core.db.DatabaseConnector

import scala.concurrent.{ExecutionContext, Future}

sealed trait UserStorage {

  def findUser(login: String): Future[Option[User]]

  def saveUser(authData: User): Future[User]

}

class JdbcUserStorage(val databaseConnector: DatabaseConnector
                     )(implicit executionContext: ExecutionContext)
  extends UserTable
    with UserStorage {

  import databaseConnector._
  import databaseConnector.account.api._

  override def findUser(login: String): Future[Option[User]] =
    db.run(users.filter(d => d.username === login || d.email === login).result.headOption)

  override def saveUser(authData: User): Future[User] =
    db.run(users.insertOrUpdate(authData)).map(_ => authData)

}

class InMemoryUserStorage extends UserStorage {

  private var state: Seq[User] = Nil

  override def findUser(login: String): Future[Option[User]] =
    Future.successful(state.find(d => d.username == login || d.email == login))

  override def saveUser(authData: User): Future[User] =
    Future.successful {
      state = state :+ authData
      authData
    }

}
