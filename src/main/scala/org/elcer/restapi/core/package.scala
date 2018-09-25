package org.elcer.restapi

package object core {

  type UserId    = String
  type AuthToken = String

  final case class AuthTokenContent(userId: UserId)

  final case class AuthData(id: UserId, username: String, email: String, password: String) {
    require(id.nonEmpty, "id.empty")
    require(username.nonEmpty, "username.empty")
    require(email.nonEmpty, "email.empty")
    require(password.nonEmpty, "password.empty")
  }

  final case class Account(id: UserId, var balance: Float, name: String) {
    require(id.nonEmpty, "id.empty")
    require(name.nonEmpty, "name.empty")
  }

  final case class AccountUpdate(firstName: Option[String] = None, lastName: Option[String] = None) {
    def merge(account: Account): Account =
      Account(account.id,account.balance, lastName.getOrElse(account.name))
  }

}
