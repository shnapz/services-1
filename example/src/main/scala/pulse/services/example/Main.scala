package pulse.services
package example

import java.io.File

import com.twitter.finagle.tracing.DefaultTracer
import com.twitter.finagle.{Http, ListeningServer, param}
import com.typesafe.config.ConfigFactory
import fs2.interop.cats._
import fs2.Task
import eu.timepit.refined.auto._
import pulse.config.Source.{Classpath, FileSource}
import pulse.config.readers._
import pulse.config.syntax._
import pulse.config.typesafe._
import pulse.services.core.ServerApp

object Main extends ServerApp[ExampleConfig] {

  override def defaultHttpPort = 9991

  override def server(args: List[String], config: ExampleConfig): Task[ListeningServer] = {
    for (params <- CliParameters(args))
      yield Http.server
        .configured(param.Label("ExampleServer"))
        .configured(param.Tracer(DefaultTracer))
        .serve(":8080", ExampleApi(config, params).apiService)
  }

  override val readConfig = for {
    _ <- conf
    maxCount <- get[Int]("threadpool.maxCount")
  } yield ExampleConfig(maxCount)

  override def configSource = FileSource(new File("example\\src\\main\\resources\\application.conf"))
}
