package server.http.encoding

import java.io.ByteArrayOutputStream
import org.scalamock.scalatest._
import org.scalatest._

class ChunkedOutputStreamTestSpec  extends FlatSpec with Matchers with MockFactory with BeforeAndAfterEach {

  "ChunkedOutputStream" should "chunk the content correctly if size less than chunk size" in {
    val out = new ByteArrayOutputStream()
    val chunked = new ChunkedOutputStream(out, 10)

    chunked.write(Array[Byte](10,11,12,13))
    chunked.finish()

    out.toByteArray.toList should be(List(52, 13, 10, 10, 11, 12, 13, 13, 10, 48, 13, 10, 13, 10))
  }

  "ChunkedOutputStream" should "chunk the content correctly if size equals chunk size" in {
    val out = new ByteArrayOutputStream()
    val chunked = new ChunkedOutputStream(out, 10)

    chunked.write(Array[Byte](10,11,12,13,14,15,16,17,18,19))
    chunked.finish()

    out.toByteArray.toList should be(List(97, 13, 10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 13, 10, 48, 13, 10, 13, 10))
  }

  "ChunkedOutputStream" should "chunk the content correctly if longer than chunk size" in {
    val out = new ByteArrayOutputStream()
    val chunked = new ChunkedOutputStream(out, 10)

    chunked.write(Array[Byte](10,11,12,13,14,15,16,17,18,19,20,21,22))
    chunked.finish()

    out.toByteArray.toList should be(List(97, 13, 10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 13, 10, 51, 13, 10, 20, 21, 22, 13, 10, 48, 13, 10, 13, 10))
  }
}