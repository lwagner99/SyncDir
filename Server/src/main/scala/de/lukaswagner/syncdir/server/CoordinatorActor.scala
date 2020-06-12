package de.lukaswagner.syncdir.server

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common._

/**
 * Coordinate all processes that have to be carried out to synchronize client and server
 */
class CoordinatorActor extends Actor with LazyLogging {

  implicit val mat: ActorMaterializer = ActorMaterializer()(context)
  val serverSendFileActor: ActorRef = context.actorOf(SendFileActor.props, "SendFileActor")
  val serverReceiveActor: ActorRef = context.actorOf(ReceiveFileActor.props, "ReceiveFileActor")

  override def receive: Receive = {
    case SyncDirRequest() =>
      val dirMeta: List[FileChecksum] = getFilesMeta
      sender ! SyncDirResponse(dirMeta)

    case downloadFileRequest: DownloadFileRequest =>
      serverSendFileActor.tell(downloadFileRequest, sender)

    case uploadFileStream: UploadFileStream =>
      serverReceiveActor.tell(uploadFileStream, sender)

    case DeleteFileRequest(filePath, transmissionID) =>
      deleteFileOnServer(filePath)
      sender() ! TransmissionsAcknowledgement(transmissionID)

    case s =>
      logger.info(s"CoordinatorActor does not support receiving method of type ${s.getClass}")
  }

  def deleteFileOnServer(filePath: String): Boolean = {
    new File(s"${Config.syncDir}/$filePath").delete()
  }

  private def getFilesMeta: List[FileChecksum] = {
    object dirIdentifier extends DirChecksumCalculating
    dirIdentifier.calculateDirChecksum(Config.syncDir, Config.syncDir.getPath)
  }
}

object CoordinatorActor {
  def props: Props =
    Props(new CoordinatorActor())
}
