package learn.part1_actors

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import learn.part1_actors.ChildActors.BankAccount.{Deposit, InitializeAccount, WithDraw}
import learn.part1_actors.ChildActors.CreditCard.{AttachToAccount, CheckStatus}
import learn.part1_actors.ChildActors.Parent.{CreateChild, TellChild}

/*
  Guardian Actors(top-level actors): Every ActorSystem have three Guardian Actors
  1. /system: system guardian - so every akka actor system has its own actors for
  managing various things for ex: managing the logging system
  2. /user: user-level guardian - every actor that we create is a child of this actor either directly or
  indirectly
  3. /: root guardian - the root guardian manages both system guardian and user-level guardian,
  the root guardian seats at the level of the actor system itself. if this actor throws an exception
  or dies in some other way the whole system is brought down.
 */
object ChildActors extends App {
  val actorSystem = ActorSystem.create("ChildActor")

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  val parentActor = actorSystem.actorOf(Props[Parent], "parent")
  parentActor ! CreateChild("child")
  parentActor ! TellChild("hey kid")
  // parentActor ! CreateChild("child2") // this is unhandled, because context changed to withChild

  /*
    Actor Selection: Find an actor by a path - if the given path is Invalid then the ActorSelection
    will contain no actor reference under the hood, and if we send message then the message will be
    delivered to the deadletter actor.
   */
  val childSelection: ActorSelection = actorSystem.actorSelection("/user/parent/child")
  // ActorSelection is wrapper over potential ActorRef that we can use to send a message
  childSelection ! "I found you"

  /*
    DANGER !!!!
    NEVER PASS MUTABLE ACTOR STATE, OR `THIS` REFERENCE, TO CHILD ACTORS
   */
  object BankAccount {
    case class Deposit(amount: Int)
    case class WithDraw(amount: Int)
    case object InitializeAccount
  }
  class BankAccount extends Actor {
    var amount = 0
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // I can pass this, but never pass `this`
      case Deposit(funds) => deposit(funds)
      case WithDraw(funds) => withdraw(funds)
    }
    def deposit(funds: Int): Unit = {
      println(s"${self.path} depositing funds $funds on top of $amount")
      amount += funds
    }
    def withdraw(funds: Int): Unit = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard {
    case class AttachToAccount(bankAccount: BankAccount) // ???????
    case object CheckStatus
  }
  class CreditCard extends Actor {
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachTo(account))
    }
    def attachTo(account: BankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your message has been processed!")
        account.withdraw(1) // because I can, and this is the major problem. We should not call
        // actor methods directly, actor are made to communicate with messages.
    }
  }

  val bankAccountRef = actorSystem.actorOf(Props[BankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)
  Thread.sleep(500)
  val creditCardSelection = actorSystem.actorSelection("/user/account/card")
  creditCardSelection ! CheckStatus
}
