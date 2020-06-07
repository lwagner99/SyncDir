package de.lukaswagner.syncdir.client

import de.lukaswagner.syncdir.common.FileChecksum

import scala.collection.mutable.ListBuffer

object Data {

  case class AsyncActionGroup(filesToDownload: ListBuffer[TransmissionMeta], filesToUpload: ListBuffer[TransmissionMeta],
                              filesToDeleteOnServer: ListBuffer[TransmissionMeta])

  case class SyncActionGroup(filesToDeleteOnClient: ListBuffer[FileChecksum], filesToUpdateHistorizedId: ListBuffer[FileChecksum])

  case class TransmissionMeta(fileMeta: FileChecksum, transmissionID: String)

}
