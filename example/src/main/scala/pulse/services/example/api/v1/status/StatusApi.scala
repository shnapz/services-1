package pulse.services.example.api.v1.status

import java.io.File
import java.nio.charset.Charset

import com.twitter.util.Future
import io.finch._
import pulse.services.example.{CliParameters, ExampleConfig}
import pulse.services.example.avro.AvroUtils

import scala.util.{Failure, Success}

/**
  * Created by Andrew on 16.03.2017.
  */
object StatusApi {
  def statusApi(config: ExampleConfig, cliParameters: CliParameters) = {
    status :+: updateStatus(cliParameters.statusAvroSchema)
  }

  def status: Endpoint[Status] =
    get("v1" :: "status") {
      Future(Ok(Status("fine")))
    }

  def updateStatus(statusAvroSchema: File): Endpoint[String] = post("v1" :: "status" :: stringBody) {
    s: String => {
      val bytes = AvroUtils.jsonToAvroBytes(s, statusAvroSchema)
      bytes match {
        case Success(b) => Future(Ok(new String(b, Charset.defaultCharset())))
        case Failure(ex) => {
          print(s"Error during json to avro serialization: ${ex.getMessage}")
          Future(InternalServerError(new Exception(ex.getMessage)))
        }
      }
    }
  }

}
