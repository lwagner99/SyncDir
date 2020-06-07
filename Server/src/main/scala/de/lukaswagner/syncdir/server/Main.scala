package de.lukaswagner.syncdir.server

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("SimpleServerSystem")
    val serverActor = system.actorOf(CoordinatorActor.props, "CoordinatorActor")
    logger.info(s"CoordinatorActor started with path: ${serverActor.path}")
  }

}
