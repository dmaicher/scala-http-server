package server.http.response.body

import java.io.{InputStream, OutputStream}
import scala.sys.process.BasicIO

class InputStreamResponseBody(inputStream: InputStream, length: Option[Long] = None) extends ResponseBody {
  override def write(outputStream: OutputStream) = {
    BasicIO.transferFully(inputStream, outputStream)
  }
  override def getLength: Option[Long] = {
    length
  }
}
