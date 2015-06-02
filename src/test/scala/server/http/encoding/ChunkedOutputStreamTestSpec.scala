package server.http.encoding

import java.io.ByteArrayOutputStream
import org.scalamock.scalatest._
import org.scalatest._

class ChunkedOutputStreamTestSpec  extends FlatSpec with Matchers with MockFactory {
  "ChunkedOutputStream" should "chunk the content correctly" in {
    val out = new ByteArrayOutputStream()
    val chunkend = new ChunkedOutputStream(out, 10)

    chunkend.write(Array[Byte](10,11,12,13,14,15,16,17,18,19,20,21,22))
    chunkend.finish()

    out.toByteArray.toList should be(List(97, 13, 10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 13, 10, 51, 13, 10, 20, 21, 22, 13, 10, 48, 13, 10, 13, 10))
  }
}