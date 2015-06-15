package server.http.request.parser

import org.scalatest._
import org.scalamock.scalatest._
import server.http.headers.{HeaderParser, Headers}

class HeaderParserTestSpec  extends FlatSpec with Matchers with MockFactory {
  val parser = new HeaderParser

  "HeaderParser" should "parse empty header correctly" in {
    parser.parse("") should be(new Headers)
  }

  "HeaderParser" should "parse single header correctly" in {
    val correct = new Headers()
    correct += "Key" -> "Value"
    parser.parse("Key:Value ") should be(correct)
    parser.parse("Key: Value") should be(correct)
    parser.parse("Key:  Value") should be(correct)
    parser.parse("Key :Value") should be(correct)
    parser.parse("Key\t:\tValue") should be(correct)
  }

  "HeaderParser" should "parse multiple headers correctly" in {
    val correct = new Headers()
    correct += "Key1" -> "Value1"
    correct += "Key2" -> "Value2"
    parser.parse("Key1:Value1 \r\nKey2: Value2") should be(correct)
    parser.parse("Key1:Value1\r\nKey2: Value2") should be(correct)
  }

  "HeaderParser" should "parse duplicate headers correctly" in {
    val correct = new Headers()
    correct += "Key" -> List("Value1", "Value2")
    parser.parse("Key: Value1\r\nKey: Value2") should be(correct)
  }

  "HeaderParser" should "parse multi-line header correctly" in {
    val correct = new Headers()
    correct += "Key" -> "Some long value across multiple lines"
    parser.parse("Key: Some long\r\n value across multiple lines") should be(correct)
    parser.parse("Key: Some long\r\n\t\tvalue across multiple lines") should be(correct)
    parser.parse("Key: Some long\r\n\tvalue across\r\n\tmultiple lines") should be(correct)
  }
}