package server.http.response

import server.http.headers.Headers

class Response(val status: Int, val body: Array[Byte] = null, val headers: Headers = new Headers) {
  def hasBody = {
    body != null && body.nonEmpty
  }
}
