package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import AverageJoes.model.workout.MachineTypes._
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike


class CustomerActorGroupTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{

  val deviceProbe: TestProbe[Device.Msg] = createTestProbe[Device.Msg]()
  val gymProbe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()
  val machineProbe: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val physicalMachineProbe: TestProbe[PhysicalMachine.Msg] = createTestProbe[PhysicalMachine.Msg]()

  val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())


  "Customer group" should {

    "review logging request" in {
      /* No customer registered */
      val groupActor = spawn(CustomerGroup("group", managerActor))
      groupActor ! CustomerLogin("customer-no","label", RUNNING, machineProbe.ref,  physicalMachineProbe.ref)

      val negativeRespMachine = machineProbe.receiveMessage()

      assert(negativeRespMachine.isInstanceOf[CustomerLogging])

      negativeRespMachine match {
        case CustomerLogging("customer-no", _, _, isLogged) => assert(isLogged === false)
        case _ => assert(false)
      }
    }

  }
}