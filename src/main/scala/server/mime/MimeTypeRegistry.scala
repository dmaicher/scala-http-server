package server.mime

import scala.collection.mutable

class MimeTypeRegistry {
  private val registry = mutable.Map[String, String](
    "css" -> "text/css",
    "js" -> "application/javascript",
    "json" -> "application/json",
    "jpeg" -> "image/jpeg",
    "jpg" -> "image/jpgeg",
    "gif" -> "image/gif",
    "png" -> "image/png",
    "svg" -> "image/svg+xml",
    "tiff" -> "image/tiff",
    "bmp" -> "image/bmp",
    "xif" -> "image/vnd.xiff",
    "ico" -> "image/x-icon",
    "txt" -> "text/plain",
    "csv" -> "text/csv",
    "xls" -> "application/vnd.ms-excel",
    "html" -> "text/html",
    "pdf" -> "application/pdf"
   )

  def getMimeTypeByExtension(extension: String): Option[String] = registry.get(extension)

  def registerMimeType(extension: String, mimeType: String): Unit = {
    registry += extension -> mimeType
  }
}
