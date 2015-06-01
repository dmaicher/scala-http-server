package server

import java.io.FileNotFoundException
import java.net.{ServerSocket, Socket, SocketException, SocketTimeoutException}
import java.util.concurrent._

import com.typesafe.scalalogging.LazyLogging
import server.handler.FastCgi.FastCgiHandler
import server.http.headers.{HeaderParser, Headers}
import server.http.request.Request
import server.http.request.parser.{ParseRequestException, RequestLineParser, RequestParser}
import server.http.response.{Response, ResponseWriter}
import server.http.{HttpMethod, HttpProtocol}
import server.router.Router

object Server {
  def main (args: Array[String]) {
    val server = new Server(8080, 75)

    /*
    server.getRouter.registerHandler(new Handler {
      override def handle(request: Request): Response = {
        new Response(200, "<img src=\"bla.png\" />")
      }
    }, "/test")
    */

    server.getRouter.registerHandler(new FastCgiHandler("/var/www/php"), "/")

    server.start()
    System.in.read
  }
}

class Server(val port: Int, val maxThreads: Int) extends LazyLogging {
  private val executor = new ThreadPoolExecutor(
    maxThreads,
    maxThreads,
    60,
    TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new WorkerThreadFactory
  )
  executor.allowCoreThreadTimeOut(true)
  private val router = new Router

  def start(): Unit = {
    val serverSocket = new ServerSocket(port)
    while(true) {
      val socket = serverSocket.accept()
      logger.debug("Accepted new incoming connection")
      executor.execute(new Worker(socket, router))
    }
  }

  def getRouter = router
}

class WorkerThreadFactory extends ThreadFactory {
  override def newThread(runnable: Runnable): Thread = {
    val t = new WorkerThread(runnable, new RequestParser(new RequestLineParser, new HeaderParser))
    t.setDaemon(true)
    t
  }
}

class WorkerThread(private val runnable: Runnable, private val requestParser: RequestParser) extends Thread(runnable) {
  def getRequestParser = requestParser
}

class Worker(val socket: Socket, router: Router) extends Runnable with LazyLogging {
  socket.setSoTimeout(5000)

  override def run(): Unit = {
    processRequest()
  }

  private def processRequest(): Unit = {
    var request: Request = null
    var response: Response = null
    try {
      request = Thread.currentThread().asInstanceOf[WorkerThread].getRequestParser.parse(socket.getInputStream)
      response = router.handle(request)
    }
    catch {
      case e: ParseRequestException =>
        logger.warn("Error pasing request", e)
        response = new Response(400)
      case e: SocketTimeoutException =>
        logger.warn("Timeout", e)
        response = new Response(408)
      case e: FileNotFoundException =>
        logger.warn("Not found", e)
        response = new Response(404)
      case e: Exception =>
        logger.error("Error handling request", e)
        response = new Response(500)
    }

    if(request == null) {
      request = new Request(HttpMethod.GET, "/", HttpProtocol.HTTP_1, new Headers)
    }

    if(!socket.isClosed) {
      try {
        logger.debug("Writing response with status %d".format(response.status))
        new ResponseWriter().write(socket.getOutputStream, request, response)
      }
      catch {
        case e: SocketException => logger.warn("Error writing response", e)
      }

      if(!request.keepAlive) {
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


