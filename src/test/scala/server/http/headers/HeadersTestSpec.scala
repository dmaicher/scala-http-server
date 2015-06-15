package server.http.headers

import org.scalamock.scalatest._
import org.scalatest._

class HeadersTestSpec  extends FlatSpec with Matchers with MockFactory {
  "Headers" should "should treat the keys case-insensitive" in {
    val headers = new Headers

    headers.put("Foo", "Bar")
    headers.get("foo") should be(Some(List("Bar")))
    headers.get("Foo") should be(Some(List("Bar")))
  }

  "Headers" should "should fold values correctly" in {
    val headers = new Headers

    headers.put("foo", List("bar1", "bar2", "bar3"))

    headers.foldValues should be(Map(
      new HeaderKey("foo") -> "bar1,bar2,bar3"
    ))

    headers.put("foo2", List("bar1", "bar2", "bar3"))

    headers.foldValuesExceptKey(new HeaderKey("foo2")) should be(Map(
      new HeaderKey("foo") -> List("bar1,bar2,bar3"),
      new HeaderKey("foo2") -> List("bar1", "bar2", "bar3")
    ))
  }
}