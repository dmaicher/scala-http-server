package server.utils

import java.io.File

class FileUtils {
  def getExtension(file: File): Option[String] = {
    getExtension(file.getName)
  }

  def getExtension(fileName: String): Option[String] = {
    ".+\\.([^.]+)$".r.findFirstMatchIn(fileName).map(_.group(1))
  }
}
