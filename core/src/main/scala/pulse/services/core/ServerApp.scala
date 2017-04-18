package pulse.services
package core

import cats.data.Kleisli
import org.log4s._
import fs2.{Stream, Task}
import com.twitter.finagle.{ListeningServer, NullServer}
import com.twitter.server._
import com.twitter.util.Await
import com.twitter.app.App
import com.twitter.logging.Logging
import pulse.common.{Runner, logging}
import pulse.common.syntax.Strategies
import pulse.config.{Conf, Source}
import pulse.config.typesafe._

abstract class ServerApp[Config >: Null] extends App
  with AdminHttpServer
  with Admin
  with Lifecycle
  with Stats
  with Strategies with Logging {

  override def allowUndefinedFlags: Boolean = true

  private implicit val L = getLogger

  private val serverState = new StateMachine[LifeCycle, Config, ListeningServer] {
    def initialState = (Init, Task(NullServer))

    val transitionFunc: TransitionFunction = {
      case Transition(Init, Started, config, _) =>
        server(args.toList, config)
      case Transition(Started, Started, config, srv) =>
        for {
          _ <- closeServer(srv)
          newSrv <- server(args.toList, config)
        } yield newSrv
      case Transition(Started, Stopped, _, srv) =>
        closeServer(srv)
    }
  }

  val readConfig: Kleisli[Task, Conf, Config]

  def configSource: Source

  def server(args: List[String], config: Config): Task[ListeningServer]

  def stopServer(): Task[ListeningServer] = {
    for {
      srv <- serverState.transitTo(Stopped, null)
      _ <- logging.info("Server is stopped")
    } yield srv
  }

  def startServer(config: Config): Stream[Task, ListeningServer] = {
    Stream.eval {
      for {
        srv <- serverState.transitTo(Started, config)
        _ <- logging.info(s"Server is started with $config")
      } yield srv
    }
  }

  def main(): Unit = {
    val runner = new ProgramRunner()
    runner.main(args)
  }

  private def closeServer(srv: ListeningServer): Task[ListeningServer] = {
    Task(Await.result(srv.close())).map(_ => srv)
  }

  private sealed trait LifeCycle

  private case object Init extends LifeCycle

  private case object Started extends LifeCycle

  private case object Stopped extends LifeCycle

  private class ProgramRunner extends Runner {
    protected override def run(args: List[String]): Task[Unit] = {
      val program = for {
        ce <- Conf.mutable(configSource)
        c <- ce.fold(fa => {
          logging.error(s"Unable to parse: ${configSource}, exception: ${fa.getCause}")
          Stream.empty
        }, fb => Stream.emit(fb))
        v <- Stream.eval(readConfig(c))
        _ <- startServer(v)
      } yield ()
      program.run
    }
  }

}
