package server.http.encoding

import java.io.{FilterOutputStream, OutputStream}

class ChunkedOutputStream(out: OutputStream) extends FilterOutputStream(out) {
  private val chunkSize = 100

  override def write(b: Array[Byte]): Unit = {
    for(i <- 0 to b.length-1 by chunkSize) {
      val curChunkLength = math.min(b.length-i, chunkSize)
      writeChunkLength(curChunkLength)
      super.write(b.slice(i, i+curChunkLength))
      writeCRLF()
    }
    writeChunkLength(0)
    writeCRLF()
  }

  private def writeChunkLength(l: Int): Unit = {
    super.write(l.toHexString.getBytes("UTF-8"))
    writeCRLF()
  }

  private def writeCRLF(): Unit = {
    super.write(13)
    super.write(10)
  }
}
