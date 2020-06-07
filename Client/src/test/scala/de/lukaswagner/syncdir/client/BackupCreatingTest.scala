package de.lukaswagner.syncdir.client

import java.io.File

import de.lukaswagner.syncdir.common.DirChecksumCalculating
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Try

class BackupCreatingTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  override def afterAll() {
    val file = new File(Config.backupDir.getAbsolutePath)
    FileUtils.deleteDirectory(file)
  }

  "BackupCreating" should "copy given dir to given path" in {
    object DirIdentifyer$ extends DirChecksumCalculating
    import DirIdentifyer$._

    object BackupCreater extends BackupCreating
    val syncedDir = Config.syncDir
    val backupDir = Config.backupDir
    val result: Try[File] = BackupCreater.copyDir(syncedDir, backupDir)
    val backupDirFileMeta = result.map(backupDir => calculateDirChecksum(backupDir, backupDir.getAbsolutePath)).getOrElse(0)
    val syncedDirFileMeta = calculateDirChecksum(syncedDir, syncedDir.getAbsolutePath)
    backupDirFileMeta shouldBe syncedDirFileMeta
  }
}
