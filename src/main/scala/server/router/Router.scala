package server.router

import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

import server.handler.Handler
import server.http.request.Request
import server.http.response.Response

class Router extends Handler {

  private val handlers = new ConcurrentHashMap[String, Handler]

  def registerHandler(handler: Handler, path: String): Unit = {
    handlers.put(path, handler)
  }

  override def handle(request: Request): Response = {
    //TODO: very naive route matching...not really useful
    handlers.get(request.location) match {
      case h: Handler => h.handle(request)
      case _ => throw new FileNotFoundException("No handler found for path "+request.location)
    }
  }
}
