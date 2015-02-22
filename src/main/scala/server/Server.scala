package server

import java.io.FileNotFoundException
import java.net.{SocketException, SocketTimeoutException, ServerSocket, Socket}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import server.handler.{StaticFileHandler, Handler}
import server.http.request.parser.{HeaderParser, RequestLineParser, ParseRequestException, RequestParser}
import server.http.{HttpMethod, HttpProtocol}
import server.http.request.Request
import server.http.response.{ResponseWriter, Response}
import server.router.Router

object Server {
  def main (args: Array[String]) {
    val server = new Server(8080, 10)

    server.getRouter.registerHandler(new Handler {
      override def handle(request: Request): Response = {
        new Response(200)
      }
    }, "/test")

    server.getRouter.registerHandler(new StaticFileHandler("/var/www/"), "/test.html")

    server.start()
    System.in.read
  }
}

class Server(val port: Int, val poolSize: Int) extends LazyLogging {
  private val threadPool = Executors.newFixedThreadPool(poolSize)
  private val router = new Router

  def start(): Unit = {
    val serverSocket = new ServerSocket(port)
    while(true) {
      val socket = serverSocket.accept()
      logger.debug("Accepted new incoming connection")
      threadPool.execute(new ServerJob(socket, router))
    }
  }

  def getRouter = router
}

class ServerJob(val socket: Socket, router: Router) extends Runnable with LazyLogging {
  socket.setSoTimeout(5000)

  override def run(): Unit = {
    var request: Request = null
    var response: Response = null
    try {
      request = new RequestParser(new RequestLineParser, new HeaderParser).parse(socket.getInputStream)

      response = {
        try {
          router.handle(request)
        }
        catch {
          case e: FileNotFoundException =>
            logger.warn("Not found", e)
            new Response(404)
          case e: Exception =>
            logger.error("Error handling request", e)
            new Response(500)
        }
      }
    }
    catch {
      case e: ParseRequestException =>
        response = new Response(400)
      case e: SocketTimeoutException =>
        response = new Response(408)
    }

    val protocol = request match {
      case r: Request => r.protocol
      case _ => HttpProtocol.HTTP_1
    }

    val writeRespBody = request match {
      case r: Request => r.method != HttpMethod.HEAD
      case _ => true
    }

    if(!socket.isClosed) {
      try {
        new ResponseWriter().write(socket.getOutputStream, response, protocol, writeRespBody)
        socket.close()
      }
      catch {
        case e: SocketException => logger.warn("Error writing response", e)
      }
    }
  }
}


