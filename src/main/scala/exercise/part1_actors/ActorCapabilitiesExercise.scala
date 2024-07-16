package exercise.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exercise.part1_actors.ActorCapabilitiesExercise.BankAccountActor.{Credit, Deposit, Statement}
import exercise.part1_actors.ActorCapabilitiesExercise.CounterActor.{Decrement, Increment, Print}
import exercise.part1_actors.ActorCapabilitiesExercise.Person.LiveTheLife

object ActorCapabilitiesExercise extends App {
  /*
     create a counter actor which has the following behavior:
      1. increment a counter
      2. decrement a counter
      3. print the current counter value
   */
  val actorSystem = ActorSystem.create("CounterActor")

  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }
  class CounterActor extends Actor {
    var counter = 0
    override def receive: Receive = {
      case Increment => counter += 1
      case Decrement => counter -= 1
      case Print => println(s"Current counter value: $counter")
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor], "CounterActor")

//  counterActor ! Increment
//  counterActor ! Print
//  counterActor ! Decrement
//  counterActor ! Print

  /*
     create a bank account as an actor
      - deposit an amount
      - withdraw an amount
      - print statement
     Instead of printing the statement reply with success or failure
   */
  object BankAccountActor {
    case class Credit(amount: Int)
    case class Deposit(amount: Int)
    case object Statement
  }
  class BankAccountActor extends Actor {
    var balance = 0
    override def receive: Receive = {
      case Credit(amount) => balance += amount
        sender() ! "Successfully credited"
      case Deposit(amount) => balance -= amount
        if (balance < 0) sender() ! "Failure: Insufficient funds with balance < 0"
        else if (amount > balance) sender() ! "Failure: Insufficient funds"
        else sender() ! "Successfully deposited"
      case Statement =>
        sender() ! s"Your current balance is $balance"
    }
  }

  object Person {
    case class LiveTheLife(actor: ActorRef)
  }
  class Person extends Actor {
    override def receive: Receive = {
      case LiveTheLife(actor) =>
        actor ! Credit(100)
        actor ! Deposit(50)
        actor ! Statement
      case message => println(message.toString)
    }
  }
  val bankAccountActor = actorSystem.actorOf(Props[BankAccountActor], "BankAccountActor")
  val personActor = actorSystem.actorOf(Props[Person], "Person")

  personActor ! LiveTheLife(bankAccountActor)
}
