package server.http.encoding

import java.io.{File, FileInputStream, InputStream, ByteArrayInputStream}

import org.scalamock.scalatest._
import org.scalatest._

import scala.collection.mutable.ArrayBuffer

class ChunkedInputStreamTestSpec  extends FlatSpec with Matchers with MockFactory {

  private def inputStreamToString(in: InputStream) = {
    val out = new ArrayBuffer[Byte]()
    var done = false
    while(!done) {
      val read = in.read()
      if(read != -1) {
        out += read.toByte
      }
      else {
        done = true
      }
    }

    new String(out.toArray, "UTF-8")
  }

  "ChunkedInputStream" should "filter wrapped InputStream correctly (1)" in {
    val in = new ByteArrayInputStream("2\r\nA \r\n5\r\nTest \r\n4\r\nfor\n\r\n6\r\nChunks\r\n0\r\n\r\n".getBytes("UTF-8"))
    inputStreamToString(new ChunkedInputStream(in)) should be("A Test for\nChunks")
  }

  "ChunkedInputStream" should "filter wrapped InputStream correctly (2)" in {
    val in = new ByteArrayInputStream("4\r\nWiki\r\n5\r\npedia\r\ne\r\n in\r\n\r\nchunks.\r\n0\r\n\r\n".getBytes("UTF-8"))
    inputStreamToString(new ChunkedInputStream(in)) should be("Wikipedia in\r\n\r\nchunks.")
  }

  "ChunkedInputStream" should "filter wrapped InputStream correctly with chunk extensions" in {
    val in = new ByteArrayInputStream("2;foo=bar;bar=foo\r\nA \r\n5;John\r\nTest \r\n4\r\nfor\n\r\n6;Doe\r\nChunks\r\n0\r\n\r\n".getBytes("UTF-8"))
    inputStreamToString(new ChunkedInputStream(in)) should be("A Test for\nChunks")
  }
}