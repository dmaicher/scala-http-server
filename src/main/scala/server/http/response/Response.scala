package server.http.response

import java.io.OutputStream
import server.http.headers.Headers
import server.http.response.body.ResponseBody

class Response(val status: Int = 200, val body: ResponseBody = null, val headers: Headers = new Headers) {
  def hasBody = {
    body != null
  }

  def writeBody(outputStream: OutputStream): Unit = {
    if(body != null) {
      body.write(outputStream)
    }
  }

  def setContentType(t: String): Unit = {
    headers += Headers.CONTENT_TYPE -> t
  }
}
