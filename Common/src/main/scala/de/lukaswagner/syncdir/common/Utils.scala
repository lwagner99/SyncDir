package de.lukaswagner.syncdir.common

import java.io.File
import java.nio.file.{Path, Paths}

object Utils {
  def createOsIndependentPath(prefixPath: String, relativeFilePath: String): Path = {
    var path = relativeFilePath.replace("\\", File.separator)
    path = path.replace("/", File.separator)
    val file = Paths.get(s"$prefixPath${File.separator}$path")
    file
  }

  def createOsIndependentStringPath(relativeFilePath: String): String = {
    var path = relativeFilePath.replace("\\", File.separator)
    path = path.replace("/", File.separator)
    path
  }

}
