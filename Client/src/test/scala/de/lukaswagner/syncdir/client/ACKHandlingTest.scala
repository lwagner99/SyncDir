package de.lukaswagner.syncdir.client

import de.lukaswagner.syncdir.client.Data.{AsyncActionGroup, TransmissionMeta}
import de.lukaswagner.syncdir.common.FileChecksum
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ACKHandlingTest extends FlatSpec with Matchers with ACKHandling {

  "ACKHandling" should "check if all acks received" in {
    val ackMap: mutable.Map[String, Boolean] = scala.collection.mutable.Map[String, Boolean]()
    ackMap.update("id", true)
    checkIfAllAcksReceived(ackMap) shouldBe true
  }

  it should "check if all acks received Part 2" in {
    val ackMap: mutable.Map[String, Boolean] = scala.collection.mutable.Map[String, Boolean]()
    ackMap.update("id", true)
    ackMap.update("id2", false)
    checkIfAllAcksReceived(ackMap) shouldBe false
  }

  it should "check if all acks received Part 3" in {
    val ackMap: mutable.Map[String, Boolean] = scala.collection.mutable.Map[String, Boolean]()
    ackMap.update("id2", false)
    ackMap.update("id", false)
    checkIfAllAcksReceived(ackMap) shouldBe false
  }

  it should "return transmission ids" in {
    val filesToDownload = new ListBuffer[TransmissionMeta]()
    val filesToUpload = new ListBuffer[TransmissionMeta]()
    val filesToDeleteOnServer = new ListBuffer[TransmissionMeta]()
    filesToDownload.append(TransmissionMeta(FileChecksum("filePath", 1234), "transmissionId"))
    filesToUpload.append(TransmissionMeta(FileChecksum("filePath3", 12345), "transmissionId2"))
    filesToDeleteOnServer.append(TransmissionMeta(FileChecksum("filePath4", 12346), "transmissionId3"))
    val asyncActionGroup = AsyncActionGroup(filesToDownload, filesToUpload, filesToDeleteOnServer)

    val result = getTransmissionIds(asyncActionGroup)
    val expectedTransmissionIds = List("transmissionId", "transmissionId2", "transmissionId3")
    result shouldBe expectedTransmissionIds
  }

  it should "return transmission ids part 2" in {
    val filesToDownload = new ListBuffer[TransmissionMeta]()
    val filesToUpload = new ListBuffer[TransmissionMeta]()
    val filesToDeleteOnServer = new ListBuffer[TransmissionMeta]()
    filesToDownload.append(TransmissionMeta(FileChecksum("filePath", 1234), "transmissionId"))
    filesToUpload.append(TransmissionMeta(FileChecksum("filePath3", 12345), "transmissionId2"))
    filesToDeleteOnServer.append(TransmissionMeta(FileChecksum("filePath4", 12346), "transmissionId2"))
    val asyncActionGroup = AsyncActionGroup(filesToDownload, filesToUpload, filesToDeleteOnServer)

    val result = getTransmissionIds(asyncActionGroup)
    val expectedTransmissionIds = List("transmissionId", "transmissionId2", "transmissionId2")
    result shouldBe expectedTransmissionIds
  }

}
