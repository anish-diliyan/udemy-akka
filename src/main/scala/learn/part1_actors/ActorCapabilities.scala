package learn.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  val actorSystem = ActorSystem("ActorCapabilities")

  class SimpleActor extends Actor {
    // each actor has a member called context, This context is a complex data structure that has reference
    // to information regarding environment in which this actor lives.
    // ex1: context.system // it can access the actor system
    // ex2: context.self // it can access the reference to itself. it is similar to 'this' in OOP.
    // akka://ActorCapabilities/user/simpleActor#93311837:  it is the path of the actor in the actor system
    // with actor unique identifier 93311837
    // ex3: context.self.path // akka://ActorCapabilities/user/simpleActor
    // ex4: we can use the context.self to send message to itself
    override def receive: Receive = {
      // sender() is a reference to the actor that sent the message, means for every actor at any
      // moment in time sender() will point to the actor that last sent the message.
      // Whenever an actor sends a message to another actor, they pass themselves as sender()
      case "Hi" => sender() ! "Hello, there" // reply to a message context.sender() ==== sender()
      case message: String => println(s"${self}: I have received $message")
      case number: Int => println(s"[Simple Actor]: I have received a number: $number")
      case SpecialMessage(contents) => println(s"[Simple Actor]: I have received something special: $contents")
      case SendMessageToYourself(content) => self ! content // context.self and self are same
      case SayHiTo(ref) => ref ! "Hi" // we can send message to another actor manish ! "Hi"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I am forwarding the message
    }
  }

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  // 1 - message can be of any type with following two condition
  // a) message must be immutable ----- currently their is no way to detect
  // immutability at compile time, but it is up to us to enforce this principle
  // b) message must be serializable (in practice, use case classes and case objects)
  simpleActor ! 42

  case class SpecialMessage(content: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors has information about their context and about themselves
  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and proud of it")

  // 3 - actor can REPLY to messages
  val anish = actorSystem.actorOf(Props[SimpleActor], "anish")
  val manish = actorSystem.actorOf(Props[SimpleActor], "manish")

  // create a new case class to use anish and manish actor references and
  // make both communicate with each other

  case class SayHiTo(ref: ActorRef)
  anish  ! SayHiTo(manish) // anish is saying hi to manish

  // 4 - dead letters
  //deadletters is a fake actor which takes care to receive messages which are not handled by any actor
  // this is the garbage pool of messages which are not handled by any actor.
  anish ! "Hi"

  // 5 - forwarding messages
  // anish ---send Hi---> manish ---pass Hi---> anshu (sending a message to original sender)
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  anish ! WirelessPhoneMessage("Hi", manish)
}
