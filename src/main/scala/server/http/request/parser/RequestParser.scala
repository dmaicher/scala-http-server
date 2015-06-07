package server.http.request.parser

import java.io.InputStream
import java.nio.charset.CodingErrorAction
import com.typesafe.scalalogging.LazyLogging
import server.http.encoding.ChunkedInputStream
import server.http.headers.{HeaderParser, Headers}
import server.http.request.Request
import scala.collection.mutable.ArrayBuffer
import scala.io.Codec

class RequestParser(val requestLineParser: RequestLineParser, val headerParser: HeaderParser) extends LazyLogging {
  private val STATE_PARSE_REQUEST_LINE = 1
  private val STATE_PARSE_HEADERS = 2
  private val STATE_PARSE_BODY = 3
  private val STATE_DONE = 4

  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  private val CRLF = List(13,10)
  private val CRLFCRLF = List(13,10,13,10)

  def parse(inputStream: InputStream): Request = {
    var requestLine: RequestLine = null
    var headers: Headers = null
    var buffer = new ArrayBuffer[Byte]
    var parseBody: () => Int = null
    var curInputStream = inputStream
    var state = STATE_PARSE_REQUEST_LINE

    var read = 0
    while(state != STATE_DONE && {read = curInputStream.read(); read != -1}) {
      buffer += read.toByte
      state match {
        case STATE_PARSE_REQUEST_LINE =>
          if(buffer.endsWith(CRLF)) {
            requestLine = requestLineParser.parse(bufferToString(buffer).trim)
            state = STATE_PARSE_HEADERS
            buffer.clear()
          }
        case STATE_PARSE_HEADERS =>
          if(buffer.endsWith(CRLFCRLF)) {
            headers = headerParser.parse(bufferToString(buffer).trim)
            logger.debug(headers.toString())
            buffer.clear()
            val transferEnc = headers.get(Headers.TRANSFER_ENCODING).map(_.toLowerCase).getOrElse("identity")
            state = {
              if(!transferEnc.equals("identity")) {
                curInputStream = new ChunkedInputStream(inputStream)
                if(!transferEnc.equals("chunked")) {
                  throw new ParseRequestException("Unsupported Transfer-encoding")
                }
                parseBody = () => STATE_PARSE_BODY
                STATE_PARSE_BODY
              }
              else if(headers.contains(Headers.CONTENT_LENGTH)) {
                val length = {
                  try {
                    headers(Headers.CONTENT_LENGTH).toInt
                  }
                  catch {
                    case e: NumberFormatException => throw new ParseRequestException("Invalid Content-Length")
                  }
                }
                parseBody = () => if (buffer.length < length) STATE_PARSE_BODY else STATE_DONE
                STATE_PARSE_BODY
              }
              else {
                STATE_DONE
              }
            }
          }
        case STATE_PARSE_BODY =>
          state = parseBody()
      }
    }

    if(requestLine == null || headers == null) {
      throw new ParseRequestException()
    }

    val req = new Request(
      requestLine.method,
      requestLine.location,
      requestLine.protocol,
      requestLine.queryString,
      headers,
      bufferToString(buffer)
    )
    logger.debug("Request body: "+req.body)

    req
  }

  private def bufferToString(b: ArrayBuffer[Byte]): String = {
    new String(b.toArray[Byte], "UTF-8")
  }
}