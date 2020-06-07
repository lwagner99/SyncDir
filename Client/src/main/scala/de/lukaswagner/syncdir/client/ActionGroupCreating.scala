package de.lukaswagner.syncdir.client

import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.client.Data.{AsyncActionGroup, SyncActionGroup, TransmissionMeta}
import de.lukaswagner.syncdir.common.FileChecksum

import scala.collection.mutable.ListBuffer

/**
 * Creates Action Group
 */
trait ActionGroupCreating extends ACKHandling with LazyLogging {

  /**
   * Creates ActionGroup. An ActionGroup describes all processes that have to be carried out to synchronize the client and server.
   *
   * @param currentFileIds  Identifier of all files currently available on the client
   * @param historicFileIds Identifier of all files that were present on the client during the last synchronization
   * @param serverIds       Identifier of all files currently available on the server
   * @return actionGroup
   */
  def createActionGroup(currentFileIds: List[FileChecksum], historicFileIds: List[FileChecksum],
                        serverIds: List[FileChecksum]): (SyncActionGroup, AsyncActionGroup) = {

    val allFileNames: List[String] = getAllFileNames(currentFileIds, historicFileIds, serverIds)
    val currentFileIdsMap: Map[String, Long] = currentFileIds.map(filMeta => filMeta.filePath -> filMeta.checkSum).toMap
    val historicFileIdsMap: Map[String, Long] = historicFileIds.map(filMeta => filMeta.filePath -> filMeta.checkSum).toMap
    val serverIdsMap: Map[String, Long] = serverIds.map(filMeta => filMeta.filePath -> filMeta.checkSum).toMap

    val filesToDownload = new ListBuffer[FileChecksum]()
    val filesToUpload = new ListBuffer[FileChecksum]()
    val filesToDeleteOnServer = new ListBuffer[FileChecksum]()
    val filesToDeleteOnClient = new ListBuffer[FileChecksum]()
    val filesToUpdateHistorizedId = new ListBuffer[FileChecksum]()

    for (fileName <- allFileNames) {
      val existsInCurrentFileIds = currentFileIds.exists(fileMeta => fileMeta.filePath == fileName)
      val existsInHistoricFileIds = historicFileIds.exists(fileMeta => fileMeta.filePath == fileName)
      val existsInServerIds = serverIds.exists(fileMeta => fileMeta.filePath == fileName)

      val currentFileID: Any = currentFileIdsMap.getOrElse(fileName, "Not Present")
      val historicFileId: Any = historicFileIdsMap.getOrElse(fileName, "Not Present")
      val serverId = serverIdsMap.getOrElse(fileName, "Not Present")

      // implement logic
      if (existsInCurrentFileIds && existsInHistoricFileIds) {
        if (currentFileID == historicFileId) {
          if (existsInServerIds) {
            if (serverId == currentFileID) {
              logger.info(s"no changes on file: $fileName, do nothing")
            }
            else {
              logger.info(s"changes to the file $fileName on server, download file")
              filesToDownload.append(FileChecksum(fileName, serverIdsMap(fileName)))
            }
          }
          else {
            logger.info(s"file $fileName was deleted on server, delete on client")
            filesToDeleteOnClient.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
          }
        }

        if (currentFileID != historicFileId) {
          if (existsInServerIds) {
            if (serverId == currentFileID) {
              logger.info(s"file $fileName was changed but is equal to file on server, updating historicId")
              filesToUpdateHistorizedId.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
            }
            if (serverId == historicFileId) {
              logger.info(s"file $fileName was changed on client, update to server")
              filesToUpload.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
            }
            if ((serverId != historicFileId) && (serverId != currentFileID)) {
              logger.info(s"file $fileName was changed on client and on server, download from server.")
              filesToDownload.append(FileChecksum(fileName, serverIdsMap(fileName)))
            }
          }
          else if (!existsInServerIds) {
            logger.info(s"file $fileName was deleted on server, delete on client")
            filesToDeleteOnClient.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
          }
        }
      }
      if (existsInCurrentFileIds && !existsInHistoricFileIds) {
        if (existsInServerIds) {
          if (serverId == currentFileID) {
            logger.info(s"file $fileName is equal on client and on server, update historicId")
            filesToUpdateHistorizedId.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
          }
          if (serverId != currentFileID) {
            logger.info(s"file $fileName is not synced with server, download from server")
            filesToDownload.append(FileChecksum(fileName, serverIdsMap(fileName)))
          }
        }
        if (!existsInServerIds) {
          logger.info(s"file $fileName is not known on server, upload to server")
          filesToUpload.append(FileChecksum(fileName, currentFileIdsMap(fileName)))
        }
      }

      if (!existsInCurrentFileIds && existsInHistoricFileIds) {
        if (existsInServerIds) {
          if (serverId == historicFileId) {
            logger.info(s"file $fileName was deleted on client, delete on server")
            filesToDeleteOnServer.append(FileChecksum(fileName, historicFileIdsMap(fileName)))
          }
          if (serverId != historicFileId) {
            logger.info(s"file $fileName was changed on server, download from server")
            filesToDownload.append(FileChecksum(fileName, serverIdsMap(fileName)))
          }
        }
        if (!existsInServerIds) {
          logger.info(s"file $fileName was deleted on server and on client, update historicId")
          filesToDeleteOnClient.append(FileChecksum(fileName, historicFileIdsMap(fileName)))
        }
      }

      if (!existsInCurrentFileIds && !existsInHistoricFileIds) {
        if (existsInServerIds) {
          logger.info(s"file $fileName is not known on client, download from server")
          filesToDownload.append(FileChecksum(fileName, serverIdsMap(fileName)))
        }
      }
    }

    val asyncFilesToDownload: ListBuffer[TransmissionMeta] = filesToDownload.map(fileMeta => TransmissionMeta(fileMeta, generateKey()))
    val asyncFilesToUpload: ListBuffer[TransmissionMeta] = filesToUpload.map(fileMeta => TransmissionMeta(fileMeta, generateKey()))
    val asyncFilesToDeleteOnServer: ListBuffer[TransmissionMeta] = filesToDeleteOnServer.map(fileMeta => TransmissionMeta(fileMeta, generateKey()))

    val syncActionGroup = SyncActionGroup(filesToDeleteOnClient, filesToUpdateHistorizedId)
    val asyncActionGroup = AsyncActionGroup(asyncFilesToDownload, asyncFilesToUpload, asyncFilesToDeleteOnServer)
    (syncActionGroup, asyncActionGroup)
  }

  private def getAllFileNames(currentFileIds: List[FileChecksum], historicFileIds: List[FileChecksum], serverIds: List[FileChecksum]) = {
    // extract all fileNames, create map
    val currentClientFileNames = currentFileIds.map(currentClientFileMeta => currentClientFileMeta.filePath)
    val historicClientFileNames = historicFileIds.map(historicClientFileMeta => historicClientFileMeta.filePath)
    val serverFileNames = serverIds.map(serverFileMeta => serverFileMeta.filePath)
    val allFileNamesWithDuplicates = currentClientFileNames ++ historicClientFileNames ++ serverFileNames
    val allFileNames = allFileNamesWithDuplicates.distinct
    allFileNames
  }
}
