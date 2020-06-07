package de.lukaswagner.syncdir.common

import java.io.File

/**
 * Calculates unique file checksums based on the file content in directory including subdirectories
 */
trait DirChecksumCalculating extends FileChecksumCalculating {

  /**
   * Calculates all file identifiers under baseDir
   *
   * @param baseDir   starting point for checksum calculation
   * @param syncedDir directory which is synchronized
   * @return
   */
  def calculateDirChecksum(baseDir: File, syncedDir: String): List[FileChecksum] = {
    val parentDirConverted = new File(baseDir.getAbsolutePath)
    val dirs: Array[File] = parentDirConverted.listFiles(_.isDirectory)
    val listOfSubFiles = dirs.map(dir => calculateDirChecksum(dir, syncedDir)).toList

    val files: Array[File] = parentDirConverted.listFiles(_.isFile)
    val fileMeta: List[FileChecksum] = files.map(file =>
      FileChecksum(file.getPath.substring(syncedDir.length + 1), calculateChecksum(file)))
      .toList
    fileMeta ++ listOfSubFiles.flatten
  }
}
