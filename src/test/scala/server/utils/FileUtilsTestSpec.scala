package server.utils

import org.scalamock.scalatest._
import org.scalatest._

class FileUtilsTestSpec  extends FlatSpec with Matchers with MockFactory {
  "getExtension" should "return correct extension" in {
    val fu = new FileUtils

    fu.getExtension("foo.bar") should be(Some("bar"))
    fu.getExtension("foo.bar.foo2.bar2") should be(Some("bar2"))
    fu.getExtension("..-") should be(Some("-"))

    fu.getExtension("foo") should be(None)
    fu.getExtension(".foo") should be(None)
    fu.getExtension("foo.") should be(None)
  }
}