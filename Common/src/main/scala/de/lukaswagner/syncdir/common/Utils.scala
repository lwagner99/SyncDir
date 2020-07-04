package de.lukaswagner.syncdir.common

import java.io.File
import java.nio.file.{Path, Paths}

object Utils {
  def createOsIndependentPath(prefixPath: String, relativeFilePath: String): Path = {
    val filePath = s"${prefixPath}${File.separator}${relativeFilePath.replaceAll("""([\\/])""", File.separator)}"
    val file = Paths.get(filePath)
    file
  }

  def createOsIndependentStringPath(relativeFilePath: String): String = {
    val filePath = s"${relativeFilePath.replaceAll("""([\\/])""", File.separator)}"
    filePath
  }

}
