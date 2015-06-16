package server.handler.FastCgi

import java.net.Socket
import com.typesafe.scalalogging.LazyLogging
import server.handler.Handler
import server.http.headers.{Headers, HeaderParser}
import server.http.request.Request
import server.http.response.Response

class FastCgiHandler(val documentRoot: String) extends Handler with LazyLogging {
  private val reader = new ResponseReader(new RecordReader, new HeaderParser)

  override def handle(request: Request): Response = {
    val socket = new Socket("127.0.0.1", 9000)
    socket.setSoTimeout(30000)

    val params = new NameValuePairList
    params.add("SERVER_PORT", "8080")
    params.add("SERVER_ADDR", "127.0.0.1")
    //params.add("REMOTE_ADDR", "127.0.0.1") //TODO
    //params.add("REMOTE_PORT", "1234") //TODO
    params.add("QUERY_STRING", request.queryString)
    params.add("DOCUMENT_ROOT", documentRoot)
    params.add("SCRIPT_FILENAME", documentRoot+"/app.php")
    params.add("SERVER_PROTOCOL", request.protocol)
    params.add("SCRIPT_NAME", "/app.php")
    params.add("REQUEST_URI", request.location)
    params.add("REQUEST_METHOD", request.method)
    params.add("SYMFONY_ENV", "dev")
    params.add("SYMFONY_DEBUG", "1")

    for((k,v) <- request.headers.foldValues) {
      params.add("HTTP_"+k.toString.replace("-", "_").toUpperCase, v)
      if(k.equals(Headers.CONTENT_TYPE)) {
        params.add("CONTENT_TYPE", v)
      }
    }

    val requestBodyBytes = request.body.getBytes("UTF-8")
    params.add("CONTENT_LENGTH", requestBodyBytes.length.toString)

    logger.debug("Proxying request via fast-cgi")
    val out = socket.getOutputStream
    out.write(new BeginRequestRecord(1).toByteArray)
    out.write(new ParameterRecord(1, params).toByteArray)
    out.write(new ParameterRecord(1).toByteArray)
    if(requestBodyBytes.length > 0) {
      requestBodyBytes.grouped(Record.MAX_CONTENT_LENGTH).foreach(g =>
        out.write(new InputRecord(1, g).toByteArray)
      )
    }
    out.write(new InputRecord(1).toByteArray)
    out.flush()

    logger.debug("Waiting for fast-cgi response")
    val response = reader.read(socket.getInputStream)
    socket.close()
    response
  }
}
