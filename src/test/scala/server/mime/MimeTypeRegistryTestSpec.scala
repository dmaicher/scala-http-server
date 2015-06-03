package server.mime

import org.scalamock.scalatest._
import org.scalatest._

class MimeTypeRegistryTestSpec  extends FlatSpec with Matchers with MockFactory {
  "MimeTypeRegistry" should "return correct type for registrered extension" in {
    val r = new MimeTypeRegistry

    r.getMimeTypeByExtension("foo") should be(None)
    r.registerMimeType("foo", "text/plain")
    r.getMimeTypeByExtension("foo") should be(Some("text/plain"))
    r.registerMimeType("foo", "text/html")
    r.getMimeTypeByExtension("foo") should be(Some("text/html"))
  }
}