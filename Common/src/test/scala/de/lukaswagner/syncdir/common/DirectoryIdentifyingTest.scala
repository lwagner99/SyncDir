package de.lukaswagner.syncdir.common

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

class DirectoryIdentifyingTest extends FlatSpec with LazyLogging {

  val testDir = "src/test/resources"
  "API" should "work" in {
    object checker extends DirChecksumCalculating
    import checker._
    val fileMeta: List[FileChecksum] = calculateDirChecksum(new File(testDir), testDir)
    fileMeta.foreach(fileMeta => logger.info(fileMeta.toString))
  }

}
