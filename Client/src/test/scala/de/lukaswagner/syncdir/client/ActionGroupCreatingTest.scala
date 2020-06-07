package de.lukaswagner.syncdir.client

import de.lukaswagner.syncdir.common.FileChecksum
import org.scalatest.{FlatSpec, Matchers}

class ActionGroupCreatingTest extends FlatSpec with Matchers {

  "ActionGroupCreating" should "return nothing if HID = SID = CID" in {
    val hids = List(
      FileChecksum("filePath", 1234)
    )
    val serverIds = List(
      FileChecksum("filePath", 1234)
    )
    val currentIds = List(
      FileChecksum("filePath", 1234)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return downloadable item if (HID = CID) && (CID != SID && HID != SID)" in {
    val hids = List(
      FileChecksum("filePath", 1234)
    )
    val serverIds = List(
      FileChecksum("filePath", 123)
    )
    val currentIds = List(
      FileChecksum("filePath", 1234)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDeleteOnServer.map(_.fileMeta) shouldBe empty
    asyncGroup.filesToUpload.map(_.fileMeta) shouldBe empty
    asyncGroup.filesToDownload.map(_.fileMeta) should equal(serverIds)
  }

  it should "return removable item for client if (HID = CID) && SID not existent" in {
    val hids = List(
      FileChecksum("filePath", 1234)
    )
    val serverIds = List()

    val currentIds = List(
      FileChecksum("filePath", 1234)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    syncGroup.filesToDeleteOnClient should equal(currentIds)
  }

  it should "return updatable item for server if (HID != CID) && (SID == HID)" in {
    val hids = List(
      FileChecksum("filePath", 1234)
    )
    val serverIds = List(
      FileChecksum("filePath", 1234)
    )

    val currentIds = List(
      FileChecksum("filePath", 12345)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload.map(_.fileMeta) should equal(currentIds)
  }

  it should "return item for updating hid if (HID != CID) && (SID == CID)" in {
    val hids = List(
      FileChecksum("filePath", 1234)
    )
    val serverIds = List(
      FileChecksum("filePath", 12345)
    )

    val currentIds = List(
      FileChecksum("filePath", 12345)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId should equal(currentIds)
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return downloadable item if (HID != CID) && (SID != CID) & (SID != HID)" in {
    val hids = List(
      FileChecksum("filePath", 123)
    )
    val serverIds = List(
      FileChecksum("filePath", 12345)
    )

    val currentIds = List(
      FileChecksum("filePath", 123456)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload.map(_.fileMeta) should equal(serverIds)
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return removable item for client if (HID != CID) && SID not existent" in {
    val hids = List(
      FileChecksum("filePath", 123)
    )
    val serverIds = List(
    )

    val currentIds = List(
      FileChecksum("filePath", 123456)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient should equal(currentIds)
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for updating hid if HID not existent && SID == CID" in {
    val hids = List(
    )
    val serverIds = List(
      FileChecksum("filePath", 123456)
    )

    val currentIds = List(
      FileChecksum("filePath", 123456)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId should equal(currentIds)
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for downloading from server if HID not existent && SID != CID" in {
    val hids = List(
    )

    val serverIds = List(
      FileChecksum("filePath", 12345)
    )

    val currentIds = List(
      FileChecksum("filePath", 123456)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload.map(_.fileMeta) should equal(serverIds)
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for uploading to server if HID not existent && SID not existent && CID exists" in {
    val hids = List(
    )
    val serverIds = List(
    )
    val currentIds = List(
      FileChecksum("filePath", 123456)
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload.map(_.fileMeta) should equal(currentIds)
  }

  it should "return item for deleting on server if CID not existent && HID == SID" in {
    val hids = List(
      FileChecksum("filePath", 123456)
    )
    val serverIds = List(
      FileChecksum("filePath", 123456)
    )
    val currentIds = List(
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer.map(_.fileMeta) should equal(serverIds)
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for downloading from server if CID not existent && HID != SID" in {
    val hids = List(
      FileChecksum("filePath", 123456)
    )
    val serverIds = List(
      FileChecksum("filePath", 12345)
    )
    val currentIds = List(
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDownload.map(_.fileMeta) should equal(serverIds)
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for updating HID on client if CID not existent && SID not existent and HID exists" in {
    val hids = List(
      FileChecksum("filePath", 123456)
    )
    val serverIds = List(
    )
    val currentIds = List(
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    syncGroup.filesToDeleteOnClient should equal(hids)
    asyncGroup.filesToDownload shouldBe empty
    asyncGroup.filesToUpload shouldBe empty
  }

  it should "return item for downloading from server if CID not existent && HID not existent and SID exists" in {
    val hids = List(
    )
    val serverIds = List(
      FileChecksum("filePath", 123456)
    )
    val currentIds = List(
    )

    object ActionGroupCreating extends ActionGroupCreating
    val (syncGroup, asyncGroup): (Data.SyncActionGroup, Data.AsyncActionGroup) = ActionGroupCreating.createActionGroup(currentIds, hids, serverIds)
    syncGroup.filesToUpdateHistorizedId shouldBe empty
    syncGroup.filesToDeleteOnClient shouldBe empty
    asyncGroup.filesToDeleteOnServer shouldBe empty
    asyncGroup.filesToDownload.map(_.fileMeta) should equal(serverIds)
    asyncGroup.filesToUpload shouldBe empty
  }
}
