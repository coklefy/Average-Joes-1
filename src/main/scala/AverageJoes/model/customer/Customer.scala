package AverageJoes.model.customer

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Customer {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new Customer(context, groupId, deviceId))

  sealed trait Command
  final case class NotifiedByMachine(requestId: Long, replyTo: ActorRef[NotifyWristband]) extends Command
  final case class NotifyWristband(requestId: Long) extends Command

  final case class CustomerAlive(requestId: Long, replyTo: ActorRef[CustomerAliveSignal]) extends Command
  final case class CustomerAliveSignal(requestId: Long) extends Command

  case object Passivate extends Command

}

class Customer(context: ActorContext[Customer.Command], groupId: String, customerId: String)
  extends AbstractBehavior[Customer.Command](context) {
  import Customer._

  println("Customer actor {"+groupId+"}-{"+customerId+"} started")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case NotifiedByMachine(id, replyTo) =>
        replyTo ! NotifyWristband(id)
        this

      case CustomerAlive(id, replyTo) =>
        replyTo ! CustomerAliveSignal(id)
        this

      case Passivate =>
        Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      println("Customer actor {"+groupId+"}-{"+customerId+"} stopped")
      this
  }

}

