package server.http.request

import server.http.Headers

class Request(val method: String, val location: String, val protocol: String, val headers: Headers, val body: String = "") {
  def keepAlive = {
    headers.getOrElse(Headers.CONNECTION, "").equalsIgnoreCase("keep-alive")
  }
}
