package server.http.request.parser

import org.scalamock.scalatest._
import org.scalatest._
import server.http.{HttpProtocol, HttpMethod}

class RequestLineParserTestSpec  extends FlatSpec with Matchers with MockFactory {
  val parser = new RequestLineParser

  "RequestLineParser" should "throw Exception for invalid Request-Lines" in {
    intercept[ParseRequestException] {
      parser.parse("")
    }
    intercept[ParseRequestException] {
      parser.parse("GET")
    }
    intercept[ParseRequestException] {
      parser.parse("POST /")
    }
    intercept[ParseRequestException] {
      parser.parse("POST / SOMETHING")
    }
    intercept[ParseRequestException] {
      parser.parse("POST HTTP/1.1")
    }
  }

  "RequestLineParser" should "parse valid Request-Lines successfully" in {
    parser.parse("GET /") should be(new RequestLine(HttpMethod.GET, "/", HttpProtocol.HTTP_1))
    parser.parse("GET / HTTP/1.0") should be(new RequestLine(HttpMethod.GET, "/", HttpProtocol.HTTP_1))
    parser.parse("POST /bla HTTP/1.1") should be(new RequestLine(HttpMethod.POST, "/bla", HttpProtocol.HTTP_1_1))
    parser.parse("HEAD /bla HTTP/1.0") should be(new RequestLine(HttpMethod.HEAD, "/bla", HttpProtocol.HTTP_1))
  }
}