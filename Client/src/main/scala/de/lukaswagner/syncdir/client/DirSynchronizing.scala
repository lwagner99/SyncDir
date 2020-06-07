package de.lukaswagner.syncdir.client

import java.io.File

import akka.actor.ActorRef
import de.lukaswagner.syncdir.client.Data.{AsyncActionGroup, SyncActionGroup, TransmissionMeta}
import de.lukaswagner.syncdir.common._

import scala.util.Try

/**
 * Contains all functions to be able to synchronize the directory
 */
trait DirSynchronizingI {
  def downloadFiles(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientReceiveFileActor: ActorRef): Unit

  def uploadFiles(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientSendFileActor: ActorRef): Unit

  def removeFilesOnClient(syncActionGroup: SyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, storedFilesDir: File): Unit

  def removeFilesOnServer(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientManagerActor: ActorRef): Unit

  def updateFileIds(syncActionGroup: SyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao): Unit
}

trait DirSynchronizing extends DirSynchronizingI {

  def downloadFiles(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientReceiveFileActor: ActorRef): Unit = {
    asyncActionGroup.filesToDownload.foreach(downloadFile(_, historizedFileIDDao, sender, clientReceiveFileActor))
  }

  def uploadFiles(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, serverCoordinatorActor: ActorRef, clientSendFileActor: ActorRef): Unit = {
    asyncActionGroup.filesToUpload.foreach(uploadFile(_, historizedFileIDDao, serverCoordinatorActor, clientSendFileActor))
  }

  def removeFilesOnClient(syncActionGroup: SyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, storedFilesDir: File): Unit = {
    syncActionGroup.filesToDeleteOnClient.foreach(removeFileOnClient(_, historizedFileIDDao, storedFilesDir))
  }

  def removeFilesOnServer(asyncActionGroup: AsyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientManagerActor: ActorRef): Unit = {
    asyncActionGroup.filesToDeleteOnServer.foreach(deleteFileOnServer(_, historizedFileIDDao, sender, clientManagerActor))
  }

  def updateFileIds(syncActionGroup: SyncActionGroup, historizedFileIDDao: HistorizedFileChecksumDao): Unit = {
    syncActionGroup.filesToUpdateHistorizedId.foreach(updateHistorizedId(_, historizedFileIDDao))
  }


  private def uploadFile(transmissionMeta: TransmissionMeta, historizedFileIDDao: HistorizedFileChecksumDao, serverCoordinatorActor: ActorRef, clientSendFileActor: ActorRef): Try[Boolean] = {
    clientSendFileActor.tell(UploadFileRequest(transmissionMeta.fileMeta.filePath, transmissionMeta.transmissionID), serverCoordinatorActor)
    historizedFileIDDao.writeEntry(transmissionMeta.fileMeta)
  }

  private def downloadFile(transmissionMeta: TransmissionMeta, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientReceiveFileActor: ActorRef): Try[Boolean] = {
    sender.tell(DownloadFileRequest(transmissionMeta.fileMeta.filePath, transmissionMeta.transmissionID), clientReceiveFileActor)
    historizedFileIDDao.writeEntry(transmissionMeta.fileMeta)
  }

  def removeFileOnClient(fileMeta: FileChecksum, historizedFileIDDao: HistorizedFileChecksumDao, storedFilesDir: File): Try[Boolean] = {
    new File(s"${storedFilesDir.getPath}/${fileMeta.filePath}").delete()
    historizedFileIDDao.deleteEntry(fileMeta)
  }

  private def deleteFileOnServer(transmissionMeta: TransmissionMeta, historizedFileIDDao: HistorizedFileChecksumDao, sender: ActorRef, clientManagerActor: ActorRef): Try[Boolean] = {
    sender.tell(DeleteFileRequest(transmissionMeta.fileMeta.filePath, transmissionMeta.transmissionID), clientManagerActor)
    historizedFileIDDao.deleteEntry(transmissionMeta.fileMeta)
  }

  def updateHistorizedId(fileMeta: FileChecksum, historizedFileIDDao: HistorizedFileChecksumDao): Try[Boolean] = {
    historizedFileIDDao.writeEntry(fileMeta)
  }
}
