package server.handler.FastCgi

import java.io.{ByteArrayOutputStream, InputStream}
import server.http.response.Response

class ResponseReader(recordReader: RecordReader) {
  def read(inputStream: InputStream): Response = {
    var record: Record = null
    val responseContent = new ByteArrayOutputStream()
    do {
      record = recordReader.read(inputStream)
      if(record != null) {
        if(record.recordType == RecordType.FCGI_STDOUT) {
          responseContent.write(record.data)
        }
        else if(record.recordType == RecordType.FCGI_STDERR) {
          //TODO: log?
        }
      }
    }
    while(record != null && !RecordType.isEndType(record.recordType))

    //TODO: parse response
    new Response(200, new String(responseContent.toByteArray, "UTF-8"))
  }
}
