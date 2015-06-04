package server.handler.FastCgi

import java.io.InputStream

class RecordReader {
  private val headerLength = 8

  def read(inputStream: InputStream): Record = {
    val header = new Array[Byte](headerLength)
    readBytes(inputStream, header, 0, headerLength)

    val contentLength = ((header(4) & 0xFF) << 8) + (header(5) & 0xFF)
    val content = new Array[Byte](contentLength)
    if(contentLength > 0) {
      readBytes(inputStream, content, 0, contentLength)
    }

    val padding = header(6) & 0xFF
    if(padding > 0) {
      readBytes(inputStream, new Array[Byte](padding), 0, padding)
    }

    new Record(header, content)
  }

  private def readBytes(in: InputStream, to: Array[Byte], start: Int, length: Int): Unit = {
    in.read(to, start, length) match {
      case -1 => throw new ReadRecordException("Premature end of stream")
      case `length` =>
      case i => readBytes(in, to, start+i, length-i)
    }
  }
}
