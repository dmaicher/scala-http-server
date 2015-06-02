package server.http.response.body

import java.io.OutputStream

class StringResponseBody(data: String, encoding: String) extends ResponseBody {
  lazy val bytes = data.getBytes(encoding)
  override def write(outputStream: OutputStream): Unit = {
   outputStream.write(bytes)
  }
  override def getLength: Option[Long] = {
    Some(bytes.length)
  }
}
