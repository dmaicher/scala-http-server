package server.http.headers

import org.scalamock.scalatest._
import org.scalatest._

class HeadersTestSpec  extends FlatSpec with Matchers with MockFactory {
  "Headers" should "should treat the keys case-insensitive" in {
    val headers = new Headers

    headers.put("Foo", "Bar")
    headers.get("foo") should be(Some("Bar"))
    headers.get("Foo") should be(Some("Bar"))
  }
}