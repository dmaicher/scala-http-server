package server.handler.FastCgi

class Record(val recordType: Byte, val requestId: Short, val data: Array[Byte]) {
  private var version: Byte = 1
  private val paddingLength: Byte = 0
  private val reserved: Byte = 0

  def this(header: Array[Byte], content: Array[Byte]) {
    this(header(1), ((header(2) << 8) & 0xFF + header(3) & 0xFF).toShort, content)
    version = header(0)
  }

  def toByteArray: Array[Byte] = {
    val contentLength = data.length
    if(contentLength > Record.MAX_CONTENT_LENGTH) {
      throw new Exception("too much data given")
    }

    val out = new Array[Byte](8+contentLength)
    out(0) = version
    out(1) = recordType
    out(2) = (requestId >> 8).toByte
    out(3) = requestId.toByte
    out(4) = (contentLength >> 8).toByte
    out(5) = contentLength.toByte
    out(6) = paddingLength
    out(7) = reserved
    System.arraycopy(data, 0, out, 8, contentLength)

    out
  }
}

object Record {
  val MAX_CONTENT_LENGTH = 65535
}

object RecordType {
  val FCGI_BEGIN_REQUEST: Byte = 1
  val FCGI_ABORT_REQUEST: Byte = 2
  val FCGI_END_REQUEST: Byte = 3
  val FCGI_PARAMS: Byte = 4
  val FCGI_STDIN: Byte = 5
  val FCGI_STDOUT: Byte = 6
  val FCGI_STDERR: Byte = 7
  val FCGI_DATA: Byte = 8
}

class BeginRequestRecord(requestId: Short, val keepAlive: Boolean = false)
  extends Record(RecordType.FCGI_BEGIN_REQUEST, requestId, Array[Byte](0,1,if(keepAlive) 1 else 0,0,0,0,0,0))

class ParameterRecord(requestId: Short, val parameters: NameValuePairList = new NameValuePairList)
  extends Record(RecordType.FCGI_PARAMS, requestId, parameters.toByteArray)

class InputRecord(requestId: Short, val input: Array[Byte] = new Array[Byte](0))
  extends Record(RecordType.FCGI_STDIN, requestId, input)
