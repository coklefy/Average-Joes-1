package AverageJoes.model.device

import AverageJoes.model.machine.PhysicalMachine
import AverageJoes.model.machine.PhysicalMachine.Msg
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
 * AC
 * @param deviceID: ID of the device. In real devices, is stored on config files
 */
class Wristband(val deviceID: String) extends Device {

  //noinspection SpellCheckingInspection
  //ToDo: è possibile uilizzare sia la receive della classe che quella della superclasse?
  override def onMessage(msg: Device.Msg): Behavior[Device.Msg] = {
    msg match{
      case Device.MsgUserLoggedInMachine(refMachineActor) => display(refMachineActor.toString()); Behaviors.same //ToDo: va passato un id o similari
      case Device.MsgNearDevice(refPM) => rfid(refPM); Behaviors.same
    }
  }

  def display (s: String): Unit ={
    println(s)
  }

  def rfid(ref: ActorRef[Msg]) : Unit ={
    ref ! PhysicalMachine.MsgRfid(deviceID)
  }

}

object Wristband{
  def startWristband(actorRefFactory: ActorRefFactory, deviceID: String): ActorRef = {
    actorRefFactory.actorOf(Props(classOf[Wristband], deviceID), deviceID)
  }
}