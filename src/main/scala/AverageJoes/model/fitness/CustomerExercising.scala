package AverageJoes.model.fitness

import AverageJoes.model.customer.CustomerActor
import AverageJoes.model.customer.CustomerActor.{ExerciseCompleted, NextMachineBooking, TrainingCompleted}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}

import scala.concurrent.duration.FiniteDuration

object CustomerExercising {
  trait Msg
  private case object TimerKey
  final case object ExerciseTiming extends Msg
  private case object ExerciseFinished extends Msg

  def apply(target: ActorRef[CustomerActor.Msg], ex: (Option[Exercise], FiniteDuration), tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new CustomerExercising(timers, target, ex, tp).initializing())
  }
}


class CustomerExercising(
                          timers: TimerScheduler[CustomerExercising.Msg],
                          target: ActorRef[CustomerActor.Msg],
                          ex: (Option[Exercise], FiniteDuration),
                          tp: TrainingProgram) {
  import CustomerExercising._

  private def initializing(): Behavior[CustomerExercising.Msg] = {
    Behaviors.receiveMessage[CustomerExercising.Msg] { message =>
      timers.startSingleTimer(TimerKey, ExerciseFinished, ex._2)
      exerciseFinished()
    }
  }

  def exerciseFinished(): Behavior[Msg] = {
    Behaviors.receiveMessage[Msg] {
      case ExerciseFinished =>
        println("[CUSTOMER ACTOR]  exercise completed ")
        if(ex._1.isDefined) target ! ExerciseCompleted(TrainingProgram(tp.customer)(tp.allExercises - ex._1.get))
        else target ! ExerciseCompleted(TrainingProgram(tp.customer)(tp.allExercises))

        Behaviors.stopped
    }
  }
}

object BookWhileExercising {
  trait Msg
  case object BookTiming extends Msg
  private case object BookAnotherMachine extends Msg
  private case object TimerKey

  def apply(target: ActorRef[CustomerActor.Msg], ex: Exercise, tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new BookWhileExercising(timers, target, ex, tp).initializing())
  }
}

class BookWhileExercising(
                           timers: TimerScheduler[BookWhileExercising.Msg],
                           target: ActorRef[CustomerActor.Msg],
                           ex: Exercise,
                           tp: TrainingProgram) {
  import BookWhileExercising._

  /** TODO: def a delta */
  private def initializing(): Behavior[BookWhileExercising.Msg] = {
    Behaviors.receiveMessage[BookWhileExercising.Msg] { message =>
      timers.startSingleTimer(TimerKey, BookAnotherMachine, new FiniteDuration(ex.parameters.duration.length-10, ex.parameters.duration.unit))
      waitUntilTimeout()
    }
  }

  def waitUntilTimeout(): Behavior[Msg] = {
    Behaviors.receiveMessage[Msg] {
      case BookAnotherMachine =>
        if(noExercisesLeft(tp)) {
          println("[CUSTOMER ACTOR]  no exercises left")
          timers.cancel(TimerKey);
          target ! TrainingCompleted()
        }
        else {
          timers.cancel(TimerKey)
          println("[CUSTOMER ACTOR]  notify for next machine booking")
          target ! NextMachineBooking(ex)
        }
        Behaviors.stopped
      case _ => waitUntilTimeout()
    }
  }

  private def noExercisesLeft(tp: TrainingProgram): Boolean = tp.allExercises.isEmpty
}
