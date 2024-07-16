package exercise.part1_actors

import akka.actor.{Actor, ActorSystem, Props}
import exercise.part1_actors.ActorBehaviourExercise.Counter.{Decrement, Increment, Print}

object ActorBehaviourExercise extends App {
  val actorSystem = ActorSystem.create("CounterSystem")
  /*
     Create the counter actor with mutable state
   */
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[$currentCount] incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"[$currentCount] decrementing")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my current count is $currentCount")
    }
  }

  val counter = actorSystem.actorOf(Props[Counter], "Counter")
  counter ! Increment //[0] incrementing
  counter ! Increment //[1] incrementing
  counter ! Decrement //[2] decrementing
  counter ! Print //[counter] my current count is 1
}
