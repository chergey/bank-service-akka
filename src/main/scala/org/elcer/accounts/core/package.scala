package org.elcer.accounts

package object core {

  type AuthToken = String

  final case class AuthTokenContent(userId: Long)

  final case class User(id: Long, username: String, email: String, password: String) {
    require(id >= 0)
    require(username.nonEmpty, "username.empty")
    require(email.nonEmpty, "email.empty")
    require(password.nonEmpty, "password.empty")
  }

  final case class Account(id: Long, var userId: Long, var balance: BigDecimal, name: String) {
    require(id >= 0)
    require(userId != null)
  }


  final case class AccountUpdate(firstName: Option[String] = None, lastName: Option[String] = None) {
    def merge(account: Account): Account =
      Account(account.id, account.userId, account.balance, lastName.getOrElse(account.name))
  }

}
