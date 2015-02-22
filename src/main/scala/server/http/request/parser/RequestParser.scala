package server.http.request.parser

import java.io.InputStream
import java.nio.charset.CodingErrorAction
import server.http.request.Request
import server.http.{Headers, HttpMethod, HttpProtocol}
import scala.io.{BufferedSource, Codec}

class RequestParser(val requestLineParser: RequestLineParser, val headerParser: HeaderParser) {
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
            requestLine = requestLineParser.parse(buffer)
            state = STATE_PARSE_HEADERS
            buffer = ""
          }
        case STATE_PARSE_HEADERS =>
          if(endsWithLineBreak(buffer, 2)) {
            headers = headerParser.parse(buffer)
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

  private def endsWithLineBreak(s: String, count: Int = 1): Boolean = s.endsWith("\r\n" * count)
}