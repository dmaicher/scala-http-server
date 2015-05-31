package server.handler.FastCgi

import java.net.Socket
import com.typesafe.scalalogging.LazyLogging
import server.handler.Handler
import server.http.headers.HeaderParser
import server.http.request.Request
import server.http.response.Response

class FastCgiHandler(val documentRoot: String) extends Handler with LazyLogging {
  private val reader = new ResponseReader(new RecordReader, new HeaderParser)
  override def handle(request: Request): Response = {
    val socket = new Socket("127.0.0.1", 9000)

    val params = new NameValuePairList
    //params.add("SERVER_PORT", "8080")
    //params.add("SERVER_ADDRESS", "127.0.0.1")
    //params.add("REMOTE_ADDR", "127.0.0.1")
    //params.add("REMOTE_HOST", "")
    //params.add("QUERY_STRING", "")
    //params.add("CONTENT_LENGTH", "0")
    params.add("DOCUMENT_ROOT" , documentRoot)
    params.add("SCRIPT_FILENAME" , documentRoot+request.location)
    //params.add( "PATH_INFO" , "/var/www/php/test.php")
    //params.add("GATEWAY_INTERFACE", "CGI/1.1")
    params.add( "SERVER_PROTOCOL" , request.protocol)
    //params.add("REMOTE_USER", "")
    params.add("SCRIPT_NAME" , request.location)
    params.add("DOCUMENT_URI" , request.location)
    params.add("REQUEST_METHOD" , request.method)
    //params.add( "PHP_SELF" , "/test.php" )
    //params.add( "HOME" , "/var/www/php" )
    //params.add( "FCGI_ROLE" , "RESPONDER" )
    //params.add( "HTTP_CONNECTION" , "keep-alive" )

    logger.debug("Proxying request via fast-cgi")
    val out = socket.getOutputStream
    out.write(new BeginRequestRecord(1).toByteArray)
    out.write(new ParameterRecord(1, params).toByteArray)
    out.write(new ParameterRecord(1).toByteArray)

    //TODO: pass real request content (could need more records depending on length)
    out.write(new InputRecord(1).toByteArray)
    out.flush()

    logger.debug("Waiting for fast-cgi response")
    val response = reader.read(socket.getInputStream)
    socket.close()
    response
  }
}
