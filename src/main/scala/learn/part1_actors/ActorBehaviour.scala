package learn.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import learn.part1_actors.ActorBehaviour.Kid.{Accept, Reject}
import learn.part1_actors.ActorBehaviour.Mom.{Ask, MomStart, Vegetable}

object ActorBehaviour extends App {
  object Kid {
    val HAPPY = "happy"
    val SAD = "sad"

    case object Accept
    case object Reject
  }

  class StatelessKid extends Actor {
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Vegetable => context.become(sadReceive)
      case Ask(_) => sender() ! Accept
    }
    def sadReceive: Receive = {
      case Ask(_) => sender() ! Reject
    }
  }

  object Mom {
    case class MomStart(kid: ActorRef)
    case class Ask(message: String)
    case object Vegetable
    case object Chocolate
  }
  class Mom extends Actor {
    override def receive: Receive = {
      case MomStart(kid) =>
        kid ! Vegetable
        kid ! Ask("do you want to play?")
      case Accept => println("Yay! my kid is happy")
      case Reject => println("My kid is sad, but he's healthy")
    }
  }
  val actorSystem = ActorSystem.create("MomAndKid")
  val kid = actorSystem.actorOf(Props[StatelessKid], "kidActor")
  val mom = actorSystem.actorOf(Props[Mom], "momActor")

  mom ! MomStart(kid)
}
