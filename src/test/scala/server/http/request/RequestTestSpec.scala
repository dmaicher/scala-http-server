package server.http.request

import org.scalamock.scalatest._
import org.scalatest._
import server.http.HttpProtocol
import server.http.headers.Headers

class RequestTestSpec  extends FlatSpec with Matchers with MockFactory {
  "Request.keepAlive" should "be correct" in {
    val headers = new Headers

    new Request("", "", "", "", headers).keepAlive should be(false)

    headers += Headers.CONNECTION -> "foo"
    new Request("", "", "", "",headers).keepAlive should be(false)

    headers += Headers.CONNECTION -> "keep-alive"
    new Request("", "", "", "", headers).keepAlive should be(false)
    new Request("", "", HttpProtocol.HTTP_1_1, "", headers).keepAlive should be(true)

    headers += Headers.CONNECTION -> "kEep-AlivE"
    new Request("", "", "", "", headers).keepAlive should be(false)
    new Request("", "", HttpProtocol.HTTP_1_1, "", headers).keepAlive should be(true)
  }

  "Request.host" should "be correct" in {
    val headers = new Headers

    new Request("", "", "", "", headers).host should be(None)

    headers += "Host"-> "localhost"
    new Request("", "", "", "", headers).host should be(Some("localhost"))

    headers += "Host"-> "localhost:8080"
    new Request("", "", "", "", headers).host should be(Some("localhost"))
  }
}