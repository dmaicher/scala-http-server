package server.http.request.parser

import java.io.InputStream
import java.nio.charset.CodingErrorAction

import server.http.Headers
import server.http.encoding.ChunkedInputStream
import server.http.request.Request

import scala.collection.mutable.ArrayBuffer
import scala.io.Codec

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
    var buffer = new ArrayBuffer[Byte]
    var parseBody: () => Int = null
    var curInputStream = inputStream
    var state = STATE_PARSE_REQUEST_LINE

    while(state != STATE_DONE) {
      val read = curInputStream.read()
      if(read == -1) {
        state = STATE_DONE
      }
      else {
        buffer += read.asInstanceOf[Byte]
        state match {
          case STATE_PARSE_REQUEST_LINE =>
            if(endsWithLineBreak(buffer)) {
              requestLine = requestLineParser.parse(bufferToString(buffer).trim)
              state = STATE_PARSE_HEADERS
              buffer.clear()
            }
          case STATE_PARSE_HEADERS =>
            if(endsWithLineBreak(buffer, 2)) {
              headers = headerParser.parse(bufferToString(buffer).trim)
              buffer.clear()
              if(!headers.getOrElse(Headers.TRANSFER_ENCODING, "identity").toLowerCase.equals("identity")) {
                //TODO: check if chunked
                curInputStream = new ChunkedInputStream(inputStream)
                parseBody = () => STATE_PARSE_BODY
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

                parseBody = () => {
                  if (buffer.length < length) {
                    STATE_PARSE_BODY
                  }
                  else {
                    STATE_DONE
                  }
                }
              }
              else {
                state = STATE_DONE
              }
            }
          case STATE_PARSE_BODY =>
            state = parseBody()
        }
      }
    }

    if(requestLine == null || headers == null) {
      throw new ParseRequestException()
    }

    new Request(requestLine.method, requestLine.location, requestLine.protocol, headers, bufferToString(buffer))
  }

  private def endsWithLineBreak(b: ArrayBuffer[Byte], count: Int = 1): Boolean = {
    if(b.size < count*2) {
      return false
    }
    for(i <- Range(0, count)) {
      if(b(b.size-2*i-2) != 13 || b(b.size-2*i-1) != 10) {
        return false
      }
    }

    true
  }

  private def bufferToString(b: ArrayBuffer[Byte]): String = {
    new String(b.toArray[Byte], "UTF-8")
  }
}