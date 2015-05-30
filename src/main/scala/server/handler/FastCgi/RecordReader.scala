package server.handler.FastCgi

import java.io.InputStream

class RecordReader {
  def read(inputStream: InputStream): Record = {
    var b: Byte = 0
    var pos = 0
    val header = new Array[Byte](8)
    var padding = 0
    var contentLength = 0
    var content: Array[Byte] = null
    var complete = false
    while(!complete) {
      val in = inputStream.read()
      if(in != -1) {
        b = in.toByte
        if(pos < header.length) {
          header(pos) = b
        }
        else {
          if(pos == header.length) {
            contentLength = ((header(4) & 0xFF) << 8) + (header(5) & 0xFF)
            content = new Array[Byte](contentLength)
            if(contentLength == 0) {
              complete = true
            }
            padding = header(6) & 0xFF
          }
          val contentPos = pos - header.length
          if(contentPos < contentLength) {
            content(contentPos) = b
          }
          if(contentPos == contentLength+padding-1) {
            complete = true
          }
        }
      }
      else {
        complete = true
      }
      pos += 1
    }

    if(pos >= header.length) {
      new Record(header, content)
    }
    else {
      null
    }
  }
}
