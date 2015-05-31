package server.http.response

import server.http.headers.Headers

class Response(val status: Int, val body: String = "", val contentType: String = "text/html", val headers: Headers = new Headers) {
  def hasBody = {
    body != null && !body.isEmpty
  }
}
