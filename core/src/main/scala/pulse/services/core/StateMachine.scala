package pulse.services.core

import java.util.concurrent.atomic.AtomicBoolean

import cats.data.Kleisli
import fs2.Task
import fs2.Task._
import org.log4s._
import pulse.common.logging
import pulse.services.core.exceptions.ServerStateException

trait StateMachine[Step, Param, State] {

  case class Transition(currentEvent: Step, nextEvent: Step, param: Param, state: State)

  //type TransitionFunction = PartialFunction[Transition, Task[State]]

  type TransitionFunction = Kleisli[Task, Transition, State]

  def initialState: (Step, Task[State])

  val transitionFunc: TransitionFunction

  def transitTo(newStep: Step, param: Param)(implicit logger: Logger): Task[State] = {
    if (!isInProgress.compareAndSet(false, true)) {
      fail(ServerStateException(s"Transition is in progress from $currentStep"))
    } else {
      mutateState(newStep, param).attempt.flatMap {
        case Right(state) => for (_ <- unsetInProgress()) yield state
        case Left(e) => for {
          _ <- unsetInProgress()
          _ <- logging.error(s"An error while transiting from $currentStep to $newStep: $e")
          f <- fail(e)
        } yield f
      }
    }
  }

  private def mutateState(newStep: Step, param: Param)(implicit logger: Logger) = {
    for {
      currentState <- currentStateTask
      newState <- transitionFunc(Transition(currentStep, newStep, param, currentState))
      _ <- {
        currentStep = newStep
        currentStateTask = now(newState)
        currentStateTask
      }
      _ <- logging.debug(s"Transited from $currentStep to $newStep with $newState")
    } yield newState
  }

  private def unsetInProgress() = {
    now {
      this.isInProgress.set(false)
    }
  }

  private var currentStep = initialState._1

  private var currentStateTask = initialState._2

  private val isInProgress = new AtomicBoolean(false)
}
