package exercise.part1_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exercise.part1_actors.ChildActorsExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

/*
  create word counter using round robbin logic for worker selection
   - send Initialize(10): to word counter master, it will create 10 10 word counter worker
   - send "akka is awesome" to word count master
     - wcm will send WordCountTask("akka is awesome") to one of it children/worker
     - children will reply with WordCountReply(3) to the wcm
   - wcm will reply 3 to the sender
*/
object ChildActorsExercise extends App {
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {
    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[Master] Initializing...")
        val childrenRefs = for(i <- 1 to nChildren) yield {
          context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        }
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }
    def withChildren(childRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[Master] I have received $text - I will send to child $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        println(s"[Master] I have received a reply for task id = $id with count $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received a task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love Akka", "Scala is super duper", "yes", "mee too")
        texts.foreach(text => master ! text)
      case count: Int => println(s"[TestActor] I received a reply : $count")
    }
  }

  val actorSystem = ActorSystem.create("WordCounter")
  val testActor = actorSystem.actorOf(Props[TestActor], "testActor")
  testActor ! "go"
}
