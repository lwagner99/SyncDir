package de.lukaswagner.syncdir.common

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class FileChecksumCalculatingTest extends FlatSpec with BeforeAndAfterEach with LazyLogging {
  val testFilePath = "src/test/resources/simple.txt"

  override def beforeEach(): Unit = {
    val file = new File(testFilePath)
    val writer = new FileWriter(file)
    writer.write("das ist eine test file und wir nach den tests wieder gelöscht")
    writer.close()
  }

  override def afterEach(): Unit = {
    val file = new File(testFilePath)
    file.delete()
  }

  private def modifyTestFile(file: File) = {
    val writer = new FileWriter(file)
    writer.append("file wurde nun modifiziert")
    writer.close()
  }

  "API" should "work with rawBytes" in {
    object FileChecking extends FileChecksumCalculating
    import FileChecking._
    val byteArray: Array[Byte] = Files.readAllBytes(Paths.get(testFilePath))
    val ret = calculateChecksum(byteArray)

    val file = new File(testFilePath)
    modifyTestFile(file)

    val byteArray2: Array[Byte] = Files.readAllBytes(Paths.get(testFilePath))
    val ret2 = calculateChecksum(byteArray2)
    assert(ret != ret2)
  }

  it should "work with FilePaths" in {
    object FileChecking extends FileChecksumCalculating
    import FileChecking._
    val path = Paths.get(testFilePath)
    val ret = calculateChecksum(path.toFile)

    val file = new File(testFilePath)
    modifyTestFile(file)

    val ret2 = calculateChecksum(path.toFile)
    assert(ret != ret2)
  }

}
