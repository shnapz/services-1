package pulse.services
package example

import java.io.File

import com.twitter.finagle.{Http, ListeningServer}
import com.typesafe.config.ConfigFactory
import core.ServerApp
import fs2.interop.cats._
import fs2.Task
import eu.timepit.refined.auto._
import pulse.config.Source.{Classpath, FileSource}
import pulse.config.readers._
import pulse.config.syntax._
import pulse.config.typesafe._
import pulse.config.{Conf, Source}

object Main extends ServerApp[ExampleConfig] {

  override def defaultHttpPort = 9991

  override def server(args: List[String], config: ExampleConfig): Task[ListeningServer] = {
    for (params <- CliParameters(args))
      yield Http.server.serve(":8080", ExampleApi(config, params).apiService)
  }

  override val readConfig = for {
    _ <- conf
    maxCount <- get[Int]("threadpool.maxCount")
  } yield ExampleConfig(maxCount)

  override def configSource = FileSource(new File("C:\\work\\scala\\pulse\\services-1\\example\\src\\main\\resources\\application.conf"))
}
