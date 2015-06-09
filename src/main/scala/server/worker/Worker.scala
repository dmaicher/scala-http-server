package server.worker

import java.io.FileNotFoundException
import java.net.{Socket, SocketException, SocketTimeoutException}
import com.typesafe.scalalogging.LazyLogging
import server.http.connection.KeepAlivePolicy
import server.http.headers.Headers
import server.http.request.Request
import server.http.request.parser.ParseRequestException
import server.http.response.Response
import server.http.{HttpMethod, HttpProtocol}

class Worker(private val socket: Socket) extends Runnable with LazyLogging {
  socket.setSoTimeout(5000)
  private var requestCount = 0

  override def run(): Unit = {
    processRequest()
  }

  private def processRequest(): Unit = {
    requestCount += 1
    val workerThread = Thread.currentThread().asInstanceOf[WorkerThread]
    var request: Request = null
    var response: Response = null
    try {
      request = workerThread.getRequestParser.parse(socket.getInputStream)
      response = workerThread.getRouter.handle(request)
    }
    catch {
      case e: ParseRequestException =>
        logger.warn("Error parsing request", e)
        response = new Response(400)
      case e: SocketTimeoutException =>
        logger.warn("Read timeout", e)
        response = new Response(408)
      case e: FileNotFoundException =>
        logger.warn("Not found", e)
        response = new Response(404)
      case e: Exception =>
        logger.error("Error handling request", e)
        response = new Response(500)
    }

    if(request == null) {
      request = new Request(HttpMethod.GET, "/", "", HttpProtocol.HTTP_1, new Headers)
    }

    if(!socket.isClosed) {

      val keepAlive: Boolean = {
        try {
          logger.debug("Writing response with status %d".format(response.status))
          workerThread.getResponseWriter.write(socket.getOutputStream, request, response, requestCount)
        }
        catch {
          case e: SocketException =>
            logger.warn("Error writing response", e)
            false
        }
      }

      if(!keepAlive) {
        logger.debug("Closing connection...")
        try {
          socket.close()
        }
        catch {
          case e: SocketException => logger.warn("Error closing socket", e)
        }
      }
      else if(socket.isConnected) {
        logger.debug("Keeping connection open...")
        this.processRequest()
      }
    }
  }
}
