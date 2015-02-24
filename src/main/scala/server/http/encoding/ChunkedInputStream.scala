package server.http.encoding

import java.io.{InputStream, FilterInputStream}
import scala.collection.mutable.ArrayBuffer

class ChunkedInputStream(in: InputStream) extends FilterInputStream(in) {
  private val STATE_PARSE_CHUNK_LENGTH = 0
  private val STATE_PARSE_CHUNK = 1
  private var chunkLength = 0
  private var buffer = new ArrayBuffer[Byte]()
  private var state = STATE_PARSE_CHUNK_LENGTH

  override def read(): Int = {
    buffer += super.read.toByte
    val endsWithCRLF = buffer.size >= 2 && buffer.last == 10 && buffer(buffer.size-2) == 13
    state match {
      case STATE_PARSE_CHUNK_LENGTH =>
        if(endsWithCRLF) {
          //only take part from left that is the actual hexadecimal length (without chunk extensions separated by semicolon (=59))
          chunkLength = Integer.parseInt(new String(buffer.dropRight(2).takeWhile(_ != 59).toArray, "UTF-8"), 16)
          if(chunkLength == 0) {
            return -1
          }
        }
      case STATE_PARSE_CHUNK =>
        if(buffer.size <= chunkLength) {
          return buffer.last
        }
    }
    if(endsWithCRLF) {
      state = state ^ 1 //flip state bit
      buffer.clear()
    }
    read()
  }
}
