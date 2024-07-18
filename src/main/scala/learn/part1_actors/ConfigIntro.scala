package learn.part1_actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/*
  Configuration control values that can tweak the behaviour of Akka, and the entire configuration
  held within the actor system.
  All the configuration in Akka starts with a akka namespace.
 */
object ConfigIntro extends App {
  // 1 - inline configuration
  val configString =
    """
      | akka {
      |   loglevel = "ERROR"
      | }
      |""".stripMargin

  // turn the configString into a configuration object
  val config = ConfigFactory.parseString(configString)

  // pass config to the construction of an actor system
  val actorSystem = ActorSystem.create("ConfigIntro", ConfigFactory.load(config))

  class LoggingActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val loggingActor = actorSystem.actorOf(Props[LoggingActor], "loggingActor")
  loggingActor ! "A message to remember"

  /*
   2 - default config file
     When you create a actor system with no configuration defined then it will use
     resources/application.conf file if it is defined.
   */
  val defaultConfigFileSystem = ActorSystem.create("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[LoggingActor], "defaultConfigFile")
  defaultConfigActor ! "Remember Me"

  /*
   3 - separate configuration in same file
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem.create("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[LoggingActor], "specialConfigActor")
  specialConfigActor ! "I am special config in same file"

  /*
    4 - separate config in another file
   */
  val separateConfig = ConfigFactory.load("secret/secret.conf")
  println(s"separate config log level: ${separateConfig.getString("akka.loglevel")}")
  val separateConfigSystem = ActorSystem.create("SeparateConfigDemo", separateConfig)
  val separateConfigActor = separateConfigSystem.actorOf(Props[LoggingActor], "separateConfigActor")
  separateConfigActor ! "I am reading config from another file"

  /*
    5 - different file format
    By default Akka takes configuration in a form of .conf files,
    but we can use Json or properties files. A ConfigFactory is smart enough to format different formats
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config log level: ${jsonConfig.getString("akka.loglevel")}")
  println(s"json config a json property: ${jsonConfig.getString("aJsonProperty")}")

  val propsConfig = ConfigFactory.load("props/propsConfiguration.properties")
  println(s"props config log level: ${propsConfig.getString("akka.loglevel")}")
  println(s"props config simple property: ${propsConfig.getString("simpleProperty")}")

}
