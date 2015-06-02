package server.http.response

import java.io.OutputStream

import server.http.headers.Headers
import server.http.response.body.ResponseBody

class Response(val status: Int, val body: ResponseBody = null, val headers: Headers = new Headers) {
  if(status == 304 && hasBody) {
    throw new Exception("response with status 304 must not have a body")
  }

  def hasBody = {
    body != null
  }

  def writeBody(outputStream: OutputStream): Unit = {
    if(body != null) {
      body.write(outputStream)
    }
  }
}
