package org.elcer.accounts.utils

import java.math.BigInteger
import java.security.MessageDigest

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.{BasicDirectives, HeaderDirectives, RouteDirectives}
import org.elcer.accounts.core.{AuthTokenContent, UserId}
import pdi.jwt._
import io.circe.parser._
import io.circe.generic.auto._

object SecurityDirectives {

  import BasicDirectives._
  import HeaderDirectives._
  import RouteDirectives._

  def authenticate(secretKey: String): Directive1[UserId] =
    headerValueByName("Token")
      .map(Jwt.decodeRaw(_, secretKey, Seq(JwtAlgorithm.HS256)))
      .map(_.toOption.flatMap(decode[AuthTokenContent](_).toOption))
      .flatMap {
        case Some(result) =>
          provide(result.userId)
        case None =>
          reject
      }

  def hex(pass: String) : String = {
     String.format("%032x", new BigInteger(1,
      MessageDigest.getInstance("SHA-256").digest(pass.getBytes("UTF-8"))))
  }

}
