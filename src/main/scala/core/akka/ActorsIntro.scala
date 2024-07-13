package core.akka

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigException.Parse

object ActorsIntro extends App {
  // create actor system
  val actorSystem = ActorSystem("firstActorSystem")

  // create an actor in actor system
  class WordCountActor extends Actor {
    var totalWords = 0

    // Receive is PartialFunction[Any => Int]
    override def receive: Receive = {
      case message: String =>
        println(s"[Word Count Actor]: I have received $message")
        totalWords += message.split(" ").length
    }
  }

  // you can not instantiate Actor by using new
  val wordCountActor = actorSystem.actorOf(Props[WordCountActor], "wordCountActor")
  // now you can communicate through actor using wordCounterActor
  wordCountActor ! "I am Learning Akka, and it's pretty damn cool"

  class AnotherActor(name: String) extends Actor {
    override def receive: Receive = {
      case message: String => println(s"I am $name and i receive $message")
    }
  }

  // Props(new AnotherActor("Anish") : This is not recommended
  // use Per
  val anotherActor = actorSystem.actorOf(Props(new AnotherActor("Anish")), "anotherActor")
  anotherActor ! "parameter"

  object AnotherActor {
    def props(name: String) = Props(new AnotherActor(name))
  }

  val anotherActor2 = actorSystem.actorOf(AnotherActor.props("Abcd"))
  anotherActor2 ! "parameter with object"
}
