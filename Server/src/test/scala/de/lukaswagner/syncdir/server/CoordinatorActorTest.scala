package de.lukaswagner.syncdir.server

import akka.actor.ActorSystem
import de.lukaswagner.syncdir.common.SyncDirRequest
import org.scalatest.FlatSpec

class CoordinatorActorTest extends FlatSpec {

  "ServerActor" should "be usable" in {
    val system = ActorSystem("SimpleServerSystem")
    val actor = system.actorOf(CoordinatorActor.props, "ServerActor")
    actor ! SyncDirRequest()
    system.terminate()
  }

}

