package org.elcer.accounts.utils

import scala.concurrent.{ExecutionContext, Future}


object MonadTransformers {

  implicit class FutureOptionMonadTransformer[A](t: Future[Option[A]])(implicit executionContext: ExecutionContext) {

    def map2[B](f: A => B, orElse: B): Future[B] =
      t.map(_.map(f).getOrElse(orElse))

    def mapT[B](f: A => B): Future[Option[B]] =
      t.map(_.map(f))

    def filterT(f: A => Boolean): Future[Option[A]] =
      t.map {
        case Some(data) if f(data) =>
          Some(data)
        case _ =>
          None
      }

    def flatMapTOuter[B](f: A => Future[B]): Future[Option[B]] =
      t.flatMap {
        case Some(data) =>
          f(data).map(Some.apply)
        case None =>
          Future.successful(None)
      }

  }

}
