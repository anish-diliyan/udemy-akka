package core.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  val actorSystem = ActorSystem("matchPattern")
  class SimpleActor extends Actor {
     println(s"${context.self} is equal to $self")
     println(s"actor path is ${self.path}")
     override def receive: Receive = {
       case "Hi" => context.sender() ! s"Hello there ${context.sender().path}"
       case number: Int => println(s"You give me number = $number")
       case name: String => println(s"You give me name = $name")
       case Person(name) => println(s"You give me person with name = $name")
       case PersonName(name) => self ! name
       case SayHiTo(actor) => actor ! "Hi"
       case ForwardTo(message, actor) => actor forward(message + "....")
     }
  }
  val numberActor = actorSystem.actorOf(Props(new SimpleActor()), "number")
  numberActor ! 20

  val stringActor = actorSystem.actorOf(Props(new SimpleActor()), "string")
  stringActor ! "Anish"

  case class Person(name: String)
  val personActor = actorSystem.actorOf(Props(new SimpleActor()), "person")
  personActor ! Person("Person - Anish")

  case class PersonName(name: String)
  val selfActor = actorSystem.actorOf(Props(new SimpleActor()), "selfCalling")
  selfActor ! PersonName("Bipin")

  case class SayHiTo(ref: ActorRef)
  val actorBob = actorSystem.actorOf(Props(new SimpleActor()), "bob")
  val actorAlice = actorSystem.actorOf(Props(new SimpleActor()), "alice")
  actorBob ! SayHiTo(actorAlice)

  // if Actor Bob to Actor deadLetters
  actorBob ! "Hi"

  // forward messages
  case class ForwardTo(message: String, ref: ActorRef)
  actorBob ! ForwardTo("Forward This", actorAlice)
}
