package server.router

import java.util.concurrent.ConcurrentHashMap

import server.handler.Handler
import server.request.Request
import server.response.Response

class Router extends Handler {

  private val handlers = new ConcurrentHashMap[String, Handler]

  def registerHandler(handler: Handler, path: String): Unit = {
    handlers.put(path, handler)
  }

  override def handle(request: Request): Response = {
    //TODO: very naive route matching...not really useful
    handlers.get(request.location) match {
      case h: Handler => h.handle(request)
      case _ => new Response(404)
    }
  }
}
