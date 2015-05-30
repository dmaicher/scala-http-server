package server.handler.FastCgi

import java.io.InputStream

class RecordReader {
  private val headerLength = 8
  def read(inputStream: InputStream): Record = {
    var pos = 0
    val header = new Array[Byte](headerLength)
    var padding = 0
    var contentLength = 0
    var content: Array[Byte] = new Array[Byte](0)
    var in: Int = 0
    while({in = inputStream.read(); in != -1} && pos < headerLength+contentLength+padding-1) {
      val b = in.toByte
      if (pos < headerLength) {
        header(pos) = b
        if(pos == 5) {
          contentLength = ((header(4) & 0xFF) << 8) + (b & 0xFF)
          content = new Array[Byte](contentLength)
        }
        if(pos == 6) {
          padding = b & 0xFF
        }
      }
      //only add to content when not in padding
      else if(pos < headerLength+contentLength) {
        content(pos - headerLength) = b
      }
      pos += 1
    }

    if(pos >= headerLength) {
      new Record(header, content)
    }
    else {
      null
    }
  }
}
