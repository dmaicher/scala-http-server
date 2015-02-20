package server

import java.net.{SocketException, SocketTimeoutException, ServerSocket, Socket}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import server.http.{HttpMethod, HttpProtocol}
import server.request.{Request, ParseRequestException, RequestParser}
import server.response.{ResponseWriter, Response}

object Server {
  def main (args: Array[String]) {
    val server = new Server(8080, 10)
    server.start()
    System.in.read
  }
}

class Server(val port: Int, val poolSize: Int) extends LazyLogging {
  private val threadPool = Executors.newFixedThreadPool(poolSize)

  def start(): Unit = {
    val serverSocket = new ServerSocket(port)
    while(true) {
      val socket = serverSocket.accept()
      logger.debug("Accepted new incoming connection")
      threadPool.execute(new ServerJob(socket))
    }
  }
}

class ServerJob(val socket: Socket) extends Runnable with LazyLogging {
  socket.setSoTimeout(5000)

  override def run(): Unit = {
    var request: Request = null
    var response: Response = null
    try {
      request = new RequestParser().parse(socket.getInputStream)
      response = new Response(200, "It works!")
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


