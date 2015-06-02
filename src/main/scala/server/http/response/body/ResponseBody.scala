package server.http.response.body

import java.io.OutputStream

trait ResponseBody {
  def write(outputStream: OutputStream)
  def getLength: Option[Long]
}
