package exercise.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorBehaviourExerciseVote extends App {
  val actorSystem = ActorSystem.create("VoteCounter")

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }
    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class Voter(citizen: Set[ActorRef])
  class VoteCounter extends Actor {
    override def receive: Receive = awaitingCommand
    def awaitingCommand: Receive = {
      case Voter(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }
    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"Vote stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val anish = actorSystem.actorOf(Props[Citizen], "anish")
  val manish = actorSystem.actorOf(Props[Citizen], "manish")
  val manisha = actorSystem.actorOf(Props[Citizen], "manisha")
  val anshu = actorSystem.actorOf(Props[Citizen], "anshu")

  anish ! Vote("Rahul")
  manish ! Vote("Modi")
  manisha ! Vote("Modi")
  anshu ! Vote("Arvind")

  val voteCounter = actorSystem.actorOf(Props[VoteCounter], "voteCounter")
  voteCounter ! Voter(Set(anish, manish, manisha, anshu))
}
