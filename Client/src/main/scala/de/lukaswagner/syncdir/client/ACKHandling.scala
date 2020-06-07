package de.lukaswagner.syncdir.client

import com.softwaremill.id.pretty.{PrettyIdGenerator, StringIdGenerator}
import de.lukaswagner.syncdir.client.Data.AsyncActionGroup

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Creating, extracting and acknowledging transmission ids
 */
trait ACKHandling {
  val generator: StringIdGenerator = PrettyIdGenerator.singleNode

  def generateKey(): String = {
    generator.nextId()
  }

  def getTransmissionIds(asyncActionGroup: AsyncActionGroup): List[String] = {
    val downloadTransmissionIDs: ListBuffer[String] =
      asyncActionGroup.filesToDownload.map(transmissionMeta => transmissionMeta.transmissionID)
    val uploadTransmissionIDs: ListBuffer[String] =
      asyncActionGroup.filesToUpload.map(transmissionMeta => transmissionMeta.transmissionID)
    val deleteFileOnServerTransmissionIDs: ListBuffer[String] =
      asyncActionGroup.filesToDeleteOnServer.map(transmissionMeta => transmissionMeta.transmissionID)
    downloadTransmissionIDs.toList ++ uploadTransmissionIDs.toList ++ deleteFileOnServerTransmissionIDs.toList
  }

  def checkIfAllAcksReceived(ackMap: mutable.Map[String, Boolean]): Boolean = {
    !ackMap.exists(keyValue => keyValue._2 == false)
  }


}
