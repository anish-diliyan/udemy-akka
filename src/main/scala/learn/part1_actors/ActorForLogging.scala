package learn.part1_actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

// Debug -> Info -> Warn -> Error
// Logging is async to minimize performance impact and implemented using actors
// Logging does not depend on a particular Logger implementation e.g SLF4J
object ActorForLogging extends App {

  class ExplicitLogger extends Actor {
    val logger: LoggingAdapter = Logging(context.system, this)
    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }

  val actorSystem = ActorSystem.create("ActorLogging")
  val loggerActor = actorSystem.actorOf(Props[ExplicitLogger], "loggerActor")
  loggerActor ! "logging a simple message"

  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      // ActorLogging provide log of type LoggingAdapter
      case message => log.info(message.toString)
      case (a, b) => log.info("Two things {} and {}", a, b)
    }
  }

  val actorWithLogging = actorSystem.actorOf(Props[ActorWithLogging], "actorWithLogging")
  actorWithLogging ! "Logging is simple by extending trait"
  actorWithLogging ! (42, 24)
}
