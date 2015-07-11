package server

import java.net.{Socket, ServerSocket}
import java.util.concurrent._
import com.typesafe.scalalogging.LazyLogging
import server.config.ServerConfig
import server.handler.FastCgi.FastCgiHandler
import server.handler.{ChainHandler, Handler, StaticFileHandler}
import server.http.connection.KeepAlivePolicy
import server.mime.MimeTypeRegistry
import server.router._
import server.router.matcher.{RequestMatcher, LocationRequestMatcher}
import server.utils.{DateUtils, FileUtils}
import server.worker.{Worker, WorkerThreadFactory}

object Server {
  def main (args: Array[String]) {
    val config = new ServerConfig
    config.port = 8080
    config.maxWorkers = 75

    val server = new Server(config)

    server.registerHandler(
      new ChainHandler(
        new StaticFileHandler(new MimeTypeRegistry, new FileUtils, new DateUtils, "/var/www/php"),
        new FastCgiHandler("/var/www/php")
      ),
      new LocationRequestMatcher("/".r)
    )

    server.start()
    System.in.read
  }
}

class Server(private val config: ServerConfig) extends LazyLogging {
  private val router = new Router
  private val keepAlivePolicy = new KeepAlivePolicy(
    config.allowHttpKeepAlive,
    config.httpKeepAliveTimeout,
    config.httpKeepAliveMaxConnections
  )
  private val executor = new ThreadPoolExecutor(
    config.maxWorkers,
    config.maxWorkers,
    60,
    TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new WorkerThreadFactory(router, keepAlivePolicy)
  )
  executor.allowCoreThreadTimeOut(true)

  def start(): Unit = {
    val serverSocket = new ServerSocket(config.port)
    while(true) {
      acceptNewSocket(serverSocket.accept())
    }
  }

  private def acceptNewSocket(socket: Socket): Unit = {
    logger.debug("Accepted new incoming client connection")
    logger.debug("Currently %d/%d (%f %%) workers busy".format(
      executor.getActiveCount,
      executor.getPoolSize,
      if(executor.getPoolSize > 0) (executor.getActiveCount/executor.getPoolSize.toFloat*100).ceil else 0.0
    ))
    if(executor.getActiveCount == config.maxWorkers) {
      logger.warn("Reached maximum limit of active workers. Request will be queued!")
    }
    executor.execute(new Worker(socket))
  }

  def registerHandler(handler: Handler, requestMatcher: RequestMatcher): Unit = {
    router.registerHandler(handler, requestMatcher)
  }
}




