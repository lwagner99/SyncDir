package de.lukaswagner.syncdir.common

import java.io.{File, FileNotFoundException, FileOutputStream, PrintWriter}

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

/**
 * DAO for file checksums
 */
trait HistorizedFileChecksumDao {
  def writeEntry(fileIdentifier: FileChecksum): Try[Boolean]

  def readAll(): Try[List[FileChecksum]]

  def deleteAll(): Try[Boolean]

  def deleteEntry(fileMeta: FileChecksum): Try[Boolean]

}

object HistorizedFileChecksumDao {

  private class HistorizedFileChecksumDaoImpl(filePath: String) extends HistorizedFileChecksumDao {
    override def readAll(): Try[List[FileChecksum]] = {
      readFile()
    }

    override def deleteAll(): Try[Boolean] = {
      writeFileMetas(List())
    }

    override def deleteEntry(fileIdentifier: FileChecksum): Try[Boolean] = {
      readFile() match {
        case Success(historizedFileMetas) => {
          for {
            filteredFileMetas <- Try(filterOldFileMeta(historizedFileMetas, fileIdentifier))
            ret <- writeFileMetas(filteredFileMetas)
          } yield ret
        }
        // file does not exist, therefore fileMeta is not available next time reading
        case Failure(exception: FileNotFoundException) =>
          Try(true)
        case Failure(exception) =>
          Try(false)
      }
    }

    override def writeEntry(fileIdentifier: FileChecksum): Try[Boolean] = {
      readFile() match {
        case Success(historizedFileMetas) => {
          for {
            filteredFileMetas <- Try(filterOldFileMeta(historizedFileMetas, fileIdentifier))
            addedFileMetas <- Try(filteredFileMetas :+ fileIdentifier.toString)
            ret <- writeFileMetas(addedFileMetas)
          } yield ret
        }
        case Failure(_) =>
          writeFileMetas(List(fileIdentifier))
      }
    }

    private def writeFileMetas(addedFileIdentifier: List[Any]): Try[Boolean] = {
      var writer: PrintWriter = null
      try {
        val file = new File(filePath)
        if (!file.exists()) {
          file.getParentFile.mkdirs()
          file.createNewFile()
        }
        writer = new PrintWriter(new FileOutputStream(filePath, false))
        addedFileIdentifier.foreach(fileIdentifier => {
          writer.write(s"${fileIdentifier.toString}")
          writer.write("\n")
        })
        writer.flush()
        writer.close()
        Try(true)
      }
      catch {
        case exception: Exception => Failure(exception)
      }

    }

    private def readFile(): Try[List[FileChecksum]] = {
      var reader: BufferedSource = null
      try {
        reader = Source.fromFile(new File(filePath))
        val fileMetas: List[FileChecksum] = reader.getLines.map(transformCSVStringToFileMeta).toList
        reader.close()
        Try(fileMetas)
      }
      catch {
        case exception: Exception => Failure(exception)
      }
    }

    private def filterOldFileMeta(historizedFileIdentifier: List[FileChecksum], fileIdentifier: FileChecksum): List[FileChecksum] = {
      historizedFileIdentifier.filter(oldFileIdentifier => oldFileIdentifier.filePath != fileIdentifier.filePath)
    }

    private def transformCSVStringToFileMeta(string: String): FileChecksum = {
      val firstIndex = string indexOf "("
      val lastIndex = string.lastIndexOf(")")
      val subString = string.substring(firstIndex + 1, lastIndex)
      val splitted = subString split ","
      val fileMeta = FileChecksum(splitted(0), splitted(1).toLong)
      fileMeta
    }
  }


  def apply(filePath: String): HistorizedFileChecksumDao = new HistorizedFileChecksumDaoImpl(filePath)

}
