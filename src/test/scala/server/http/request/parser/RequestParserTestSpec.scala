package server.http.request.parser

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.zip.GZIPOutputStream
import javax.print.attribute.standard.Compression

import org.scalamock.scalatest._
import org.scalatest._
import server.http.encoding.ChunkedOutputStream
import server.http.headers.{HeaderParser, Headers}
import server.http.{HttpMethod, HttpProtocol}

class RequestParserTestSpec  extends FlatSpec with Matchers with MockFactory {
  val requestLineParser = mock[RequestLineParser]
  val headerParser = mock[HeaderParser]
  val requestParser = new RequestParser(requestLineParser, headerParser)

  "RequestParser" should "parses Request-Line and Header parts of InputStream correctly" in {
    val requestLine = new RequestLine(HttpMethod.GET, "/", "", HttpProtocol.HTTP_1_1)
    val headers = new Headers

    requestLineParser.parse _ expects("Request-line") returns requestLine
    headerParser.parse _ expects("Headers1\r\nHeaders2") returns headers

    val input = new ByteArrayInputStream("Request-line\r\nHeaders1\r\nHeaders2\r\n\r\n".getBytes("UTF-8"))

    val request = requestParser.parse(input)
    request.method should be(requestLine.method)
    request.location should be(requestLine.location)
    request.protocol should be(requestLine.protocol)
    request.headers should be(headers)
    request.body should be("")
  }

  "RequestParser" should "ignore body without Content-Length or Transfer-Encoding header" in {
    val body = "Some_really_nice_body_öäü"
    val headers = new Headers

    requestLineParser.parse _ expects * returns new RequestLine(HttpMethod.GET, "/", "", HttpProtocol.HTTP_1_1)
    headerParser.parse _ expects * returns headers

    val input = new ByteArrayInputStream(("RL\r\nH\r\n\r\n"+body).getBytes("UTF-8"))

    val request = requestParser.parse(input)
    request.body should be("")
  }

  "RequestParser" should "parses body for Content-Length header correctly" in {
    val body = "Some_really_nice_body_öäü"
    val headers = new Headers
    headers += "Content-Length" -> body.getBytes("UTF-8").length.toString

    requestLineParser.parse _ expects * returns new RequestLine(HttpMethod.GET, "/", "", HttpProtocol.HTTP_1_1)
    headerParser.parse _ expects * returns headers

    val input = new ByteArrayInputStream(("RL\r\nH\r\n\r\n"+body).getBytes("UTF-8"))

    val request = requestParser.parse(input)
    request.body should be(body)
  }

  "RequestParser" should "parses body for Transfer-Encoding chunked header correctly" in {
    val body = "Some_really_nice_body_öäü"
    val headers = new Headers
    headers += "Transfer-Encoding" -> "chunked"

    requestLineParser.parse _ expects * returns new RequestLine(HttpMethod.GET, "/", "", HttpProtocol.HTTP_1_1)
    headerParser.parse _ expects * returns headers

    val outByte = new ByteArrayOutputStream()
    new ChunkedOutputStream(outByte, 2).write(body.getBytes("UTF-8"))

    val input = new ByteArrayInputStream("RL\r\nH\r\n\r\n".getBytes("UTF-8") ++ outByte.toByteArray)

    val request = requestParser.parse(input)
    request.body should be(body)
  }
}