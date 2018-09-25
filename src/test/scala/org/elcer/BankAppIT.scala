package org.elcer

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.elcer.restapi.BankApp
import org.elcer.utils.InMemoryPostgresStorage
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend

import scala.concurrent.Future

class BankAppIT extends BaseServiceTest {

  InMemoryPostgresStorage
  implicit val sttpBackend: SttpBackend[Future, Source[ByteString, Any]] = AkkaHttpBackend()

  "Service" should {

    "bind on port successfully and answer on health checks" in {
      awaitForResult(for {
        serverBinding <- BankApp.startApplication()
        healthCheckResponse <- sttp.get(uri"http://localhost:9000/healthcheck").send()
        _ <- serverBinding.unbind()
      } yield {
        healthCheckResponse.code shouldBe 200
        healthCheckResponse.body shouldBe Right("OK")
      })
    }

  }

}
