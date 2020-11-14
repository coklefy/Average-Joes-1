package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.controller.GymController.Msg.{CustomerList, CustomerRegistered}
import AverageJoes.model.customer.CustomerManager.{RequestCustomerCreation, RequestCustomerList}
import AverageJoes.model.device.{Device, Wristband}
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val probe = createTestProbe[GymController.Msg]()
  val deviceActor: ActorRef[Device.Msg] = spawn(Wristband("device-01"))


  "Customer manager actor" should {

    "create one customer" in {
      val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())

      managerActor ! RequestCustomerCreation("customer", probe.ref, deviceActor)
      val registered = probe.receiveMessage()

      assert(registered.isInstanceOf[CustomerRegistered])
    }
  }

  "return the same customer, for the same customerId creation request" in {
    val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())

    managerActor ! RequestCustomerCreation("customer-same", probe.ref, deviceActor)
    val registered1 = probe.receiveMessage()

    managerActor ! RequestCustomerCreation("customer-same", probe.ref, deviceActor)
    val registered2 = probe.receiveMessage()

    assert(registered1 == registered2)

    managerActor ! RequestCustomerCreation("customer-other", probe.ref, deviceActor)
    val registered3 = probe.receiveMessage()

    assert(registered1 == registered2)
    assert(registered2 !== registered3)
    assert(registered1 !== registered3)
  }

  "reply the customers list" in {
    val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())

    managerActor ! RequestCustomerCreation("customer-1", probe.ref, deviceActor)
    probe.receiveMessage()
    managerActor ! RequestCustomerCreation("customer-2", probe.ref, deviceActor)
    probe.receiveMessage()
    managerActor ! RequestCustomerCreation("customer-3", probe.ref, deviceActor)
    probe.receiveMessage()

    managerActor ! RequestCustomerList(probe.ref)

    val response = probe.receiveMessage()

    response match {
      case CustomerList(customers) =>
        assert(true)
        assert(customers.size === 3)
      case _ => assert(false)
    }
  }

}