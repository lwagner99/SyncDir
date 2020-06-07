package de.lukaswagner.syncdir.client

import java.io.File
import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils

import scala.util.Try

/**
 * Creates backup of given dir
 */
trait BackupCreating extends LazyLogging {
  def copyDir(dirToCopy: File, backupDir: File): Try[File] = {
    val now = LocalDateTime.now()
    val backupFile = new File(s"${backupDir.getAbsolutePath}/${now.toString}")

    for {
      _ <- Try(FileUtils.copyDirectory(dirToCopy, backupFile))
      ret <- Try(backupFile)
      _ <- Try(logger.info(s"created backup under dir ${backupDir.getAbsolutePath}/${now.toString}"))
    } yield ret
  }

}
