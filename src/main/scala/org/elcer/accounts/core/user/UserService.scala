package org.elcer.accounts.core.user

import io.circe.generic.auto._
import io.circe.syntax._
import org.elcer.accounts.core.{User, AuthToken, AuthTokenContent}
import org.elcer.accounts.utils.MonadTransformers._
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class UserService(authDataStorage: UserStorage,
                   secretKey: String
                 )(implicit executionContext: ExecutionContext) {

  import org.elcer.accounts.utils.SecurityDirectives._

  def signIn(login: String, password: String): Future[Option[AuthToken]] = {
    val hexed = hex(password)

    authDataStorage
      .findUser(login)
      .filterT(_.password == hexed)
      .mapT(authData => encodeToken(authData.id))
  }

  def signUp(login: String, email: String, password: String): Future[AuthToken] = {
    val hexed = hex(password)

    authDataStorage
      .saveUser(User(Random.nextInt(Int.MaxValue), login, email, hexed))
      .map(authData => encodeToken(authData.id))
  }

  private def encodeToken(userId: Long): AuthToken =
    Jwt.encode(AuthTokenContent(userId).asJson.noSpaces, secretKey, JwtAlgorithm.HS256)

}
