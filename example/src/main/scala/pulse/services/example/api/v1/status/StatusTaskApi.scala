package pulse.services.example.api.v1.status

import java.io.File
import java.nio.charset.Charset

import fs2.Task
import io.finch._
import pulse.common.syntax.Strategies
import pulse.services.core.Component
import pulse.services.example.{CliParameters, ExampleConfig}
import pulse.services.example.avro.AvroUtils

import scala.util.{Failure, Success}
/**
  * Created by Andrew on 16.03.2017.
  */
object StatusTaskApi extends Component with Strategies {
  def statusApi(config: ExampleConfig, cliParameters: CliParameters) = {
    status :+: updateStatus(cliParameters.statusAvroSchema)
  }

  def status: Endpoint[Status] =
    get("v1" :: "status") {
      _transformTask(Task.now(Ok(Status("fine"))))
    }

  def updateStatus(statusAvroSchema: File): Endpoint[String] = post("v1" :: "status" :: stringBody) {
    s: String => {
      val bytes = AvroUtils.jsonToAvroBytes(s, statusAvroSchema)
      bytes match {
        case Success(b) => _transformTask(Task.now(Ok(new String(b, Charset.defaultCharset()))))
        case Failure(ex) => {
          print(s"Error during json to avro serialization: ${ex.getMessage}")
          _transformTask(Task.fail(new Exception(ex.getMessage)))
        }
      }
    }
  }
}
