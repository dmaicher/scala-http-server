package server.http.response

import java.io.ByteArrayOutputStream
import org.scalamock.scalatest._
import org.scalatest._
import server.http.headers.Headers
import server.http.{HttpProtocol, HttpMethod}
import server.http.connection.KeepAlivePolicy
import server.http.request.Request

class ResponseWriterTestSpec  extends FlatSpec with Matchers with MockFactory {

  "ResponseWriter" should "write correct response status line" in {
    val writer = new ResponseWriter(new KeepAlivePolicy(true, 1, 1))
    val out = new ByteArrayOutputStream()

    writer.write(out, new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1_1), new Response(200), 0)
    new String(out.toByteArray, "UTF-8") should startWith("HTTP/1.1 200 OK\r\n")
    out.reset()

    writer.write(out, new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1_1), new Response(500), 0)
    new String(out.toByteArray, "UTF-8") should startWith("HTTP/1.1 500 Internal Server Error\r\n")
    out.reset()

    writer.write(out, new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1), new Response(400), 0)
    new String(out.toByteArray, "UTF-8") should startWith("HTTP/1.0 400 Bad Request\r\n")
    out.reset()
  }

  "ResponseWriter" should "write correct Connection + Keep-Alive header and return correct keepAlive flag" in {
    val resp = new Response()

    val writer = new ResponseWriter(new KeepAlivePolicy(true, 3, 2))

    val out = new ByteArrayOutputStream()

    var keepAlive = writer.write(out, new Request(HttpMethod.GET, "/"), resp, 0)
    keepAlive should be(false)
    var responseContent = new String(out.toByteArray, "UTF-8").toLowerCase
    responseContent should not include "connection: keep-alive"
    out.reset()

    var requestHeaders = new Headers
    requestHeaders += Headers.CONNECTION -> "Keep-Alive"
    keepAlive = writer.write(out, new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1_1, "", requestHeaders), resp, 0)
    keepAlive should be(true)
    responseContent = new String(out.toByteArray, "UTF-8").toLowerCase
    responseContent should include("connection: keep-alive")
    responseContent should include("keep-alive: timeout=3, max=2")
    out.reset()
  }

  "ResponseWriter" should "fold all headers but Set-Cookie in response" in {
    val writer = new ResponseWriter(new KeepAlivePolicy(true, 1, 1))
    val out = new ByteArrayOutputStream()

    val response = new Response(200)
    response.headers += "X-Foo" -> List("Bar1", "Bar2")
    response.headers += Headers.SET_COOKIE -> List("Bar1", "Bar2")

    writer.write(out, new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1_1), response, 0)
    val responseContent = new String(out.toByteArray, "UTF-8")
    responseContent should be("HTTP/1.1 200 OK\r\nX-Foo: Bar1,Bar2\r\nSet-Cookie: Bar1\r\nSet-Cookie: Bar2\r\nConnection: close\r\n\r\n")
    out.reset()
  }
}