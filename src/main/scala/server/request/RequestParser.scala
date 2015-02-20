package server.request

import java.io.InputStream
import java.nio.charset.CodingErrorAction
import server.http.{HttpMethod, Headers, HttpProtocol}
import scala.io.{BufferedSource, Codec}

class RequestParser {
  private val STATE_PARSE_REQUEST_LINE = 1
  private val STATE_PARSE_HEADERS = 2
  private val STATE_PARSE_BODY = 3
  private val STATE_DONE = 4

  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  def parse(inputStream: InputStream): Request = {
    var requestLine: RequestLine = null
    var headers: Headers = null
    var buffer = ""
    var state = STATE_PARSE_REQUEST_LINE

    val in = new BufferedSource(inputStream)

    while(state != STATE_DONE && in.hasNext) {
      buffer += in.next()
      state match {
        case STATE_PARSE_REQUEST_LINE =>
          if(endsWithLineBreak(buffer)) {
            requestLine = new RequestLineParser().parse(buffer)
            state = STATE_PARSE_HEADERS
            buffer = ""
          }
        case STATE_PARSE_HEADERS =>
          if(endsWithLineBreak(buffer, 2)) {
            headers = new HeaderParser().parse(buffer)
            //state = STATE_PARSE_BODY
            //TODO: move to state STATE_PARSE_BODY once its implemented
            state = STATE_DONE
            buffer = ""
          }
        case STATE_PARSE_BODY =>
        //TODO: stop when content length reached
      }
    }

    if(requestLine == null || headers == null) {
      throw new ParseRequestException()
    }

    new Request(requestLine.method, requestLine.location, requestLine.protocol, headers, buffer)
  }

  private def endsWithLineBreak(s: String, count: Int = 1): Boolean = {
    s.endsWith("\r\n" * count)
  }
}

case class RequestLine(method: String, location: String, protocol: String)
class ParseRequestException(val reason: String = "") extends Exception(reason)

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

class HeaderParser {
  def parse(s: String): Headers  = {
    val headers = new Headers
    s.split("[\r\n]+").map(l => l.split(":", 2).map(_.trim)).foreach(a => {
      if(a.size == 2) {
        headers += a(0) -> a(1)
      }
    })
    headers
  }
}