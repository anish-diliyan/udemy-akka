package play_ground

import akka.actor.ActorSystem

object PlayGround extends App {
  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem.name) // HelloAkka
}
