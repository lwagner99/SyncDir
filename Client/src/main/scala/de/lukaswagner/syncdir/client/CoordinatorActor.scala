package de.lukaswagner.syncdir.client

import akka.actor.{Actor, Props, _}
import akka.stream.ActorMaterializer
import de.lukaswagner.syncdir.client.Data.{AsyncActionGroup, SyncActionGroup}
import de.lukaswagner.syncdir.common._

import scala.collection.mutable
import scala.util.Try

/**
 * Coordinate all processes that have to be carried out to synchronize client and server
 */
class CoordinatorActor extends Actor
  with DirChecksumCalculating with DirSynchronizing with ActionGroupCreating with ACKHandling {

  implicit val mat: ActorMaterializer = ActorMaterializer()(context)
  val clientReceiveFileActor: ActorRef = context.actorOf(ReceiveFileActor.props, "ReceiveFileActor")
  val clientSendFileActor: ActorRef = context.actorOf(SendFileActor.props, "SendFileActor")
  val ackMap: mutable.Map[String, Boolean] = scala.collection.mutable.Map[String, Boolean]()

  override def receive: Receive = {
    case SyncDirResponse(serverFilesMeta) =>
      syncSharedDir(serverFilesMeta)

    case TransmissionsAcknowledgement(transmissionID) =>
      ackMap.update(transmissionID, true)
      terminateMachineIfAllACKSReceived()

    case s =>
      logger.info(s"CoordinatorActor does not support receiving method of type ${s.getClass}")
  }

  /**
   * Synchronizes client-server directory
   *
   * @param serverIds
   */
  private def syncSharedDir(serverIds: List[FileChecksum]): Unit = {
    val currentFileIds: List[FileChecksum] = calculateDirChecksum(Config.syncDir, Config.syncDir.getPath)
    val historizedFileChecksumDao: HistorizedFileChecksumDao = HistorizedFileChecksumDao(Config.historizedFileChecksumPath)
    val historicFileChecksums: Try[List[FileChecksum]] = historizedFileChecksumDao.readAll()

    val (syncActionGroup, asyncActionGroup): (SyncActionGroup, AsyncActionGroup) =
      createActionGroup(currentFileIds, historicFileChecksums.getOrElse(List()), serverIds)

    getTransmissionIds(asyncActionGroup).foreach(transmissionId => ackMap += (transmissionId -> false))

    downloadFiles(asyncActionGroup, historizedFileChecksumDao, sender(), clientReceiveFileActor)
    uploadFiles(asyncActionGroup, historizedFileChecksumDao, sender(), clientSendFileActor)
    removeFilesOnServer(asyncActionGroup, historizedFileChecksumDao, sender(), this.self)
    removeFilesOnClient(syncActionGroup, historizedFileChecksumDao, Config.syncDir)
    updateFileIds(syncActionGroup, historizedFileChecksumDao)
    terminateMachineIfAllACKSReceived()
  }

  private def terminateMachineIfAllACKSReceived(): Unit = {
    if (checkIfAllAcksReceived(ackMap)) {
      logger.info("all ACKS received")
      System.exit(0)
    }
  }
}

object CoordinatorActor {
  def props: Props = Props(new CoordinatorActor())
}

