package server.http.request.parser

import server.http.{HttpMethod, HttpProtocol}

case class RequestLine(method: String, location: String, queryString: String, protocol: String)

class RequestLineParser {
  def parse(s: String): RequestLine = {
    val parts = s.trim.split("[ ]+")
    if(parts.size < 2 || parts.size > 3) {
      throw new ParseRequestException("Invalid Request-Line")
    }

    val method = parts(0).toUpperCase
    if(!HttpMethod.exists(method)) {
      throw new ParseRequestException("Invalid HTTP Method")
    }

    val protocol = {
      if(parts.size == 2) {
        if(method != HttpMethod.GET) {
          throw new ParseRequestException("Invalid Request-Line")
        }
        HttpProtocol.HTTP_1
      }
      else {
        val protocol = parts(2).toUpperCase
        if(!HttpProtocol.exists(protocol)) {
          throw new ParseRequestException("Unsupported HTTP Protocol")
        }
        protocol
      }
    }

    val locationParts = parts(1).split("\\?", 2)
    new RequestLine(
      method,
      locationParts(0),
      if(locationParts.length > 1) locationParts(1) else "",
      protocol
    )
  }
}
