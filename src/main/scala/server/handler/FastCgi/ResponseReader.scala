package server.handler.FastCgi

import java.io.{ByteArrayOutputStream, InputStream}
import com.typesafe.scalalogging.LazyLogging
import server.http.headers.HeaderParser
import server.http.response.Response

class ResponseReader(recordReader: RecordReader, val headerParser: HeaderParser) extends LazyLogging {
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

    getResponse(new String(stdOutContent.toByteArray, "UTF-8"))
  }

  private def getResponse(stdOutContent: String): Response = {
    val parts = stdOutContent.split("\r\n\r\n", 2).map(_.trim)
    val headers = headerParser.parse(parts(0))
    val body = if(parts.length == 2) parts(1) else ""
    val status = "[0-9]{3}".r.findFirstIn(headers.remove("Status").getOrElse("200")).getOrElse("200")

    new Response(status.toInt, body)
  }
}
