package server.handler.FastCgi

import java.io.InputStream

class RecordReader {
  private val headerLength = 8
  def read(inputStream: InputStream): Record = {
    val header = new Array[Byte](headerLength)
    if(inputStream.read(header, 0, headerLength) == -1) {
      return null
    }

    val contentLength = ((header(4) & 0xFF) << 8) + (header(5) & 0xFF)
    val content = new Array[Byte](contentLength)
    if(contentLength > 0) {
      if(inputStream.read(content, 0, contentLength) == -1) {
        return null
      }
    }

    val padding = header(6) & 0xFF
    if(padding > 0) {
      if(inputStream.read(new Array[Byte](padding), 0, padding) == -1) {
        return null
      }
    }

    new Record(header, content)
  }
}
