package pulse.services.core

import cats.data.Kleisli
import cats.implicits._
import fs2.interop.cats._
import fs2._
import com.twitter.app.App
import com.twitter.finagle.{ListeningServer, NullServer}
import com.twitter.server._
import fs2.Task._
import org.log4s._
import pulse.common.syntax.Strategies
import pulse.common.{Runner, logging}
import pulse.config.typesafe._
import pulse.config.{Conf, ReaderConf, Source}
import pulse.services.core.exceptions.ServerStateException

abstract class ServerApp[Config >: Null] extends App
  with AdminHttpServer
  with Admin
  with Lifecycle
  with Stats
  with Strategies {

  override def allowUndefinedFlags: Boolean = true

  private implicit val L = getLogger

  private val serverState = new StateMachine[LifeCycle, Config, ListeningServer] {

    val transitionFunc: TransitionFunction = Kleisli {
      case Transition(Init, Started, config, _) =>
        server(args.toList, config)
      case Transition(Started, Started, config, srv) =>
        for {
          _ <- closeServer(srv)
          newSrv <- server(args.toList, config)
        } yield newSrv
      case Transition(Started, Stopped, _, srv) =>
        closeServer(srv)
      case notSupported => fail(ServerStateException(s"Transition from ${notSupported.currentEvent} to ${notSupported.nextEvent} is not supported"))
    }

    def initialState = (Init, now(NullServer))

  }

  val readConfig: ReaderConf[Config]

  def configSource: Source

  def server(args: List[String], config: Config): Task[ListeningServer]

  def stopServer(config: Config): Task[Unit] = {
    serverState.transitTo(Stopped, config) >>= (_ => logging.info("Server is stopped"))
  }

  def startServer(config: Config): Stream[Task, ListeningServer] =
    Stream.eval {
      for {
        srv <- serverState.transitTo(Started, config)
        _ <- logging.info(s"Server is started with $config")
      } yield srv
    }

  def main(): Unit = {
    val runner = new ProgramRunner()
    runner.main(args)
  }

  private def closeServer(srv: ListeningServer): Task[ListeningServer] = {
    toTask(srv.close().map(_ => srv))
  }

  private sealed trait LifeCycle

  private case object Init extends LifeCycle

  private case object Started extends LifeCycle

  private case object Stopped extends LifeCycle

  private final class ProgramRunner extends Runner {
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


