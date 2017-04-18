package pulse.services.example

import java.io.File

import cats.data.Kleisli
import fs2.Task
import pulse.config._
import scopt.OptionParser

object CliParameters {

  def apply(args: List[String]): Task[CliParameters] = {
    val parser = new OptionParser[CliParameters]("common") {
      opt[Boolean]("use-task-api")
        .action((b, c) => c.copy(useTaskApi = b))
        .text("Pass true or false for use-task-api parameter")

      opt[File]("status-avro-schema")
        .action((f, c) => c.copy(statusAvroSchema = f))
        .text("Pass valid file path to status.avsc")
    }

    parser.parse(args, CliParameters()) match {
      case Some(config) => Task.now(config)
      case None => Task.fail(new InvalidCmdArgsException("Invalid arguments passed"))
    }
  }
}

case class CliParameters(statusAvroSchema: File = new File("."), useTaskApi: Boolean = false)
