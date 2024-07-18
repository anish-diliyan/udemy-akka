package learn.part2_testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import learn.part2_testing.BasicSpec.{BlackHole, LabTestActor, SimpleActor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random

/*
  testActor:- it is a actor used to communicated with the actor that we want to test.
  ImplicitSender:- it helps to pass testActor implicitly as sender for every single message.
 */
class BasicSpec
    extends TestKit(ActorSystem("BasicSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor], "echoActor")
      val message = "Hello Test"
      echoActor ! message
      /*
        possible error --- assertion failed: timeout (3 seconds) during expectMsg while waiting
        we can configure timeout in millis using config akka.test.single-expect-default
      */
      expectMsg(message)
    }
  }

  "A black hole actor" should {
    "no message expected" in {
      val blackHoleActor = system.actorOf(Props[BlackHole], "blackHoleActor")
      val message = "Hello Test"
      blackHoleActor ! message
      // i will wait for 1 second and expect for no message in this duration
      expectNoMessage(1.second)
    }
  }

  "A lab test actor" should {
    // If you have stateful actor that you want to clear after every test, you will create
    // inside every test.
    val labTestActor = system.actorOf(Props[LabTestActor], "labTestActor")
    "turn a string into upper case" in {
      labTestActor ! "i love akka"
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
    }
    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }
    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }
    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages: Seq[Any] = receiveN(2)
      // Now free to do more complicate assertion
      assert(messages == List("Scala", "Akka"))
    }
    "reply with cool tech in fancy way" in {
      labTestActor ! "favoriteTech"
      // no need to provide implementation of case, because we only care about receiving here
      // this pattern is useful with case classes or more complex pattern
      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }
  }
}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" => if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" => sender() ! "Scala"; sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}