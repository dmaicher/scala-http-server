package server.http.response.body

import java.io.OutputStream

class ByteArrayResponseBody(data: Array[Byte]) extends ResponseBody {
  override def write(outputStream: OutputStream): Unit = {
   outputStream.write(data)
  }

  override def getLength: Option[Long] = {
    Some(data.length)
  }
}
