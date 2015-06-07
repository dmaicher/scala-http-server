package server

import java.io.FileNotFoundException
import java.net.{ServerSocket, Socket, SocketException, SocketTimeoutException}
import java.util.concurrent._
import com.typesafe.scalalogging.LazyLogging
import server.handler.FastCgi.FastCgiHandler
import server.handler.StaticFileHandler
import server.http.headers.{HeaderParser, Headers}
import server.http.request.Request
import server.http.request.parser.{ParseRequestException, RequestLineParser, RequestParser}
import server.http.response.{Response, ResponseWriter}
import server.http.{HttpMethod, HttpProtocol}
import server.mime.MimeTypeRegistry
import server.router._
import server.router.matcher.LocationRequestMatcher
import server.utils.FileUtils

object Server {
  def main (args: Array[String]) {
    val server = new Server(8080, 75)

    server.getRouter.registerHandler(
      new StaticFileHandler(new MimeTypeRegistry, new FileUtils, "/var/www/php"),
      new LocationRequestMatcher("/(js|css|images|bundles)/".r)
    )

    server.getRouter.registerHandler(
      new FastCgiHandler("/var/www/php"),
      new LocationRequestMatcher("/".r)
    )

    server.start()
    System.in.read
  }
}

class Server(private val port: Int, private val maxThreads: Int) extends LazyLogging {
  private val router = new Router
  private val executor = new ThreadPoolExecutor(
    maxThreads,
    maxThreads,
    60,
    TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new WorkerThreadFactory(router)
  )
  executor.allowCoreThreadTimeOut(true)

  def start(): Unit = {
    val serverSocket = new ServerSocket(port)
    while(true) {
      val socket = serverSocket.accept()
      logger.debug("Accepted new incoming connection")
      logger.debug("Currently %d/%d (%f %%) workers busy".format(
        executor.getActiveCount,
        executor.getPoolSize,
        if(executor.getPoolSize > 0)(executor.getActiveCount/executor.getPoolSize.toFloat*100).ceil else 0.0
      ))
      if(executor.getActiveCount == maxThreads) {
        logger.warn("Reached maximum limit of active workers. Request will be queued!")
      }
      executor.execute(new Worker(socket))
    }
  }

  def getRouter = router
}

class WorkerThreadFactory(private val router: Router) extends ThreadFactory {
  override def newThread(runnable: Runnable): Thread = {
    val t = new WorkerThread(runnable, new RequestParser(new RequestLineParser, new HeaderParser), new ResponseWriter(), router)
    t.setDaemon(true)
    t
  }
}

class WorkerThread(
    private val runnable: Runnable,
    private val requestParser: RequestParser,
    private val responseWriter: ResponseWriter,
    private val router: Router
  ) extends Thread(runnable) {
  def getRequestParser = requestParser
  def getResponseWriter = responseWriter
  def getRouter = router
}

class Worker(private val socket: Socket) extends Runnable with LazyLogging {
  socket.setSoTimeout(5000)

  override def run(): Unit = {
    processRequest()
  }

  private def processRequest(): Unit = {
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
      try {
        logger.debug("Writing response with status %d".format(response.status))
        workerThread.getResponseWriter.write(socket.getOutputStream, request, response)
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


