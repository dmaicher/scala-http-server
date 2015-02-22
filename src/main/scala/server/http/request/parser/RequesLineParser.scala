package server.http.request.parser

import server.http.{HttpProtocol, HttpMethod}

case class RequestLine(method: String, location: String, protocol: String)

class RequestLineParser {
  def parse(s: String): RequestLine = {
    val parts = s.replaceAll("[\r\n]+", "").split("[ ]+")
    if(parts.size != 3) {
      throw new ParseRequestException("Invalid Request-Line")
    }

    val method = parts(0).toUpperCase
    if(!HttpMethod.exists(method)) {
      throw new ParseRequestException("Invalid HTTP Method")
    }

    val protocol = parts(2).toUpperCase
    if(!HttpProtocol.exists(protocol)) {
      throw new ParseRequestException("Unsupported HTTP Protocol")
    }

    new RequestLine(method, parts(1), protocol)
  }
}
