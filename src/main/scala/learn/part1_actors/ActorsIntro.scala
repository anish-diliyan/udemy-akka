package learn.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {
  // part1 - actor system: The actor system is a very heavy-weight object/data structure that controls
  // the number of threads under the hood and manages the thread pool for us.

  // it's recommended to have only one actor system per application, unless we have good reason to
  // create multiple actor systems.

  // Also the name of the actor system has restriction, so it must contain only alpha numeric.
  val actorSystem = ActorSystem("FirstActorSystem")
  println(actorSystem.name)

  /*
    1. Actors are uniquely identified:  (like Indian Adhar number) by a path, which is a combination
       of the actor system name and the actor name.
    2. Messages are asynchronous, so the order of execution is not guaranteed. that means you send a
       message when you need and actor will reply back to you when they can.
    3. Each Actor has a unique behavior, or a unique way of processing the message.
    4. Actors are really encapsulated and can't access the variables of other actors.
   */
  // part2_testing - create actors, that will count words
  class WordCounterActor extends Actor {
    // internal data
    var totalWords = 0
    // behavior: message handler that akka will invoke when it receives a message
    // you can use Alias Receive in place of PartialFunction[Any, Unit]
    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] : Received message: $message")
        totalWords += message.split(" ").length
      case msg => println(s"Unexpected message: $msg")
    }
  }

  // part3 - create actor instance
  // The difference between normal object and actor is you can not create instance of actor using new.
  // In order to have a proper actor, you need to use actor system to create it.
  // "wordCounterActor" is the actor name. It is good idea name your actor with some meaningful name.
  // ActorReference is a reference to the actor. Akka exposes this reference to you so that you can not
  // call into the actual actor instance that akka creates behind the scene.
  val wordCounterActor: ActorRef = actorSystem.actorOf(Props[WordCounterActor], "wordCounterActor")

  // part4 - communicate with actor
  // We can communicate with actor through the actor reference. using ! (tell) method
  wordCounterActor ! "I am learning Akka" // This is how you send a message to the actor.
}
