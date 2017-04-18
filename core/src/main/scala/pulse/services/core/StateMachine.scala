package pulse.services.core

import org.log4s._

import pulse.common.exceptions._
import pulse.common.logging
import pulse.common._

import java.util.concurrent.atomic.AtomicBoolean

import fs2.{Strategy, Task}

import scala.util.control.NonFatal

/**
  * Created by Andrew on 12.03.2017.
  */

trait StateMachine[Step, Param, State] {

  case class Transition(currentEvent: Step, nextEvent: Step, param: Param, state: State)

  type TransitionFunction = PartialFunction[Transition, Task[State]]

  def initialState: (Step, Task[State])

  val transitionFunc: TransitionFunction

  def transitTo(newStep: Step, param: Param)(implicit logger: Logger, strategy: Strategy): Task[State] = {
    if (!isInProgress.compareAndSet(false, true)) {
      Task.fail(new Error(s"Transition is in progress from $currentStep"))
    } else {
      purelyTransitTo(currentState)(newStep, param)
        .flatMap(newState => {
          logger.debug(s"Transited from $currentStep to $newStep with $newState")
          currentStep = newStep
          currentState = Task(newState)
          isInProgress.set(false)
          currentState
        }).handleWith({ case NonFatal(e) =>
        logging.error(s"An error while transiting from $currentStep to $newStep $e")
        isInProgress.set(false)
        Task.fail(e)
      })
    }
  }

  def purelyTransitTo(currentState: Task[State])(newStep: Step, param: Param): Task[State] =
    currentState.flatMap(state =>
      transitionFunc.lift(Transition(currentStep, newStep, param, state)) match {
        case Some(transitResult) => transitResult
        case None => Task.fail(new Error(s"Transition from $currentStep to $newStep is not supported"))
      }
    )

  private var currentStep = initialState._1

  private var currentState = initialState._2

  private val isInProgress = new AtomicBoolean(false)
}