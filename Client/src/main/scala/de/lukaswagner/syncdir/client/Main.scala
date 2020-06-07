package de.lukaswagner.syncdir.client

import java.io.File

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.SyncDirRequest

import scala.util.Try

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    // ----- Creates copy of syncedDir ------
    object BackupCreater extends BackupCreating
    val result: Try[File] = BackupCreater.copyDir(Config.syncDir, Config.backupDir)
    result.failed.foreach { exp =>
      logger.info(exp.toString)
      throw new RuntimeException("Backup creating failed")
    }

    // ------- init actor system
    val actorSystem = ActorSystem("SimpleClientSystem")
    val clientCoordinatorActor = actorSystem.actorOf(CoordinatorActor.props, "CoordinatorActor")
    val serverCoordinatorActor: ActorSelection =
      actorSystem.actorSelection(s"akka.tcp://SimpleServerSystem@${Config.akkaUrl}/user/CoordinatorActor")
    logger.info(s"ClientCoordinatorActor started with path: ${clientCoordinatorActor.path}")

    // ------- starts synchronization of syncedDir
    sendSyncDirRequest(clientCoordinatorActor, serverCoordinatorActor)
  }


  def sendSyncDirRequest(clientActor: ActorRef, serverActor: ActorSelection): Unit = {
    serverActor.tell(SyncDirRequest(), clientActor)
  }
}
