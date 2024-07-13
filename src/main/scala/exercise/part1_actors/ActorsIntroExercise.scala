package exercise.part1_actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntroExercise extends App {
  val actorSystem = ActorSystem("ActorsIntroExercise")

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hello, my name is $name")
      case _ => println("I do not understand!")
    }
  }

  // Create a person actor, where Actor constructor takes a String parameter.
  // Props(new Person("Anish")): this is valid, but not recommended
  val personActor = actorSystem.actorOf(Props(new Person("Anish")),  "personActor")
  personActor ! "Hi"

  // best practice: declare a companion object for the actor class
  object Person {
    def props(name: String): Props = Props(new Person(name))
  }
  val personActor2 = actorSystem.actorOf(Person.props("Anish2"),  "personActor2")
  personActor2 ! "Hi"
}
