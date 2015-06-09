package server.http.request

import server.http.HttpProtocol
import server.http.headers.Headers

class Request(
  val method: String,
  val location: String,
  val protocol: String = HttpProtocol.HTTP_1_1,
  val queryString: String = "",
  val headers: Headers = new Headers,
  val body: String = "") {

  def keepAlive = {
    protocol == HttpProtocol.HTTP_1_1 && headers.containsWithValue(Headers.CONNECTION, "keep-alive")
  }

  def host: Option[String] = headers.get("Host").map(_.replaceFirst(":[0-9]+$", ""))
}
