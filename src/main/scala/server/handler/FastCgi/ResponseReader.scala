package server.handler.FastCgi

import java.io.{ByteArrayOutputStream, InputStream}
import com.typesafe.scalalogging.LazyLogging
import server.http.headers.HeaderParser
import server.http.response.Response
import server.http.response.body.ByteArrayResponseBody

class ResponseReader(recordReader: RecordReader, val headerParser: HeaderParser) extends LazyLogging {
  private val headerBodySeparator = List(13,10,13,10) //CRLF
  def read(inputStream: InputStream): Response = {
    var record: Record = null
    val stdOutContent = new ByteArrayOutputStream()
    val stdErrContent = new ByteArrayOutputStream()

    while({record = recordReader.read(inputStream); record != null} && record.recordType != RecordType.FCGI_END_REQUEST) {
      if(record.recordType == RecordType.FCGI_STDOUT) {
        stdOutContent.write(record.data)
      }
      else if(record.recordType == RecordType.FCGI_STDERR) {
        stdErrContent.write(record.data)
      }
    }

    if(stdErrContent.size() > 0) {
      logger.error("Received error stream via fast-cgi: %s".format(new String(stdErrContent.toByteArray, "UTF-8")))
    }

    //no stdout content at all?
    if(stdOutContent.size() == 0) {
      return new Response(500)
    }

    getResponse(stdOutContent.toByteArray)
  }

  private def getResponse(stdOut: Array[Byte]): Response = {
    var prev = List[Byte]()
    val headerBytes = stdOut.takeWhile(b => {prev = prev :+ b; !prev.endsWith(headerBodySeparator)}).dropRight(3)
    val headers = headerParser.parse(new String(headerBytes, "UTF-8"))
    val status = "[0-9]{3}".r.findFirstIn(headers.getOrElse("Status", "200")).getOrElse("200")

    new Response(
      status.toInt,
      new ByteArrayResponseBody(stdOut.slice(prev.length, stdOut.length)),
      headers
    )
  }
}
