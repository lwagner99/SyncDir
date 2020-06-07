package de.lukaswagner.syncdir.common

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import scala.util.{Failure, Success, Try}

class HistorizedFileChecksumDaoTest extends FlatSpec with LazyLogging {
  val csvPath = "src/test/resources/csv.csv"

  "HistorizedFileIDDao" should "write entry" in {
    val fileMeta = FileChecksum("filePath3", 2999)
    val dao: HistorizedFileChecksumDao = HistorizedFileChecksumDao.apply(csvPath)
    val ret: Try[Boolean] = dao.writeEntry(fileMeta)
    ret match {
      case Success(value) => logger.info("Success")
      case Failure(exception) => logger.info(exception.toString)
    }
  }

  it should "delete all entries" in {
    val dao: HistorizedFileChecksumDao = HistorizedFileChecksumDao.apply(csvPath)
    val ret: Try[Boolean] = dao.deleteAll()
  }

  it should "read all entry" in {
    val dao: HistorizedFileChecksumDao = HistorizedFileChecksumDao.apply(csvPath)
    val fileMetas: Try[List[FileChecksum]] = dao.readAll()
  }

  it should "delete entry" in {
    val dao: HistorizedFileChecksumDao = HistorizedFileChecksumDao.apply(csvPath)
    val fileMeta = FileChecksum("filePath2", 2999)
    val ret = dao.deleteEntry(fileMeta)
  }
}
