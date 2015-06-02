package server.http.encoding

import java.io.{FilterOutputStream, OutputStream}

class ChunkedOutputStream(out: OutputStream, chunkSize: Int) extends FilterOutputStream(out) {
  private val buffer = new Array[Byte](chunkSize)
  private var currentBufferPos = 0
  private val CRLF = Array[Byte](13, 10)

  override def write(b: Array[Byte], start: Int, length: Int): Unit = {
    if(length <= 0) {
      return
    }
    if(currentBufferPos + length > chunkSize) {
      val remainingInBuffer = chunkSize - currentBufferPos
      System.arraycopy(b, start, buffer, currentBufferPos, remainingInBuffer)
      currentBufferPos = chunkSize
      flushBuffer()
      write(b, start+remainingInBuffer, length-remainingInBuffer)
    }
    else {
      System.arraycopy(b, start, buffer, currentBufferPos, length)
      currentBufferPos += length
      if(currentBufferPos == chunkSize) {
        flushBuffer()
      }
    }
  }

  override def write(i: Int): Unit = {
    buffer(currentBufferPos) = i.toByte
    currentBufferPos += 1
    if(currentBufferPos == chunkSize) {
      flushBuffer()
    }
  }

  private def flushBuffer(): Unit = {
    if(currentBufferPos > 0) {
      writeChunkLength(currentBufferPos)
      out.write(buffer, 0, currentBufferPos)
      writeCRLF()
      currentBufferPos = 0
    }
  }

  private def writeChunkLength(l: Int): Unit = {
    out.write(l.toHexString.getBytes("UTF-8"))
    writeCRLF()
  }

  private def writeCRLF(): Unit = {
    out.write(CRLF, 0, CRLF.length)
  }

  def finish(): Unit = {
    flushBuffer()
    writeChunkLength(0)
    writeCRLF()
  }
}
