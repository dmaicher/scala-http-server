package server.router

import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap
import server.handler.Handler
import server.http.request.Request
import server.http.response.Response
import scala.collection.JavaConversions._

class Router extends Handler {

  private val handlers = new ConcurrentHashMap[String, Handler]

  def registerHandler(handler: Handler, path: String): Unit = {
    handlers.put(path, handler)
  }

  override def handle(request: Request): Response = {
    for(key <- handlers.keys()) {
      if(request.location.startsWith(key)) {
        return handlers.get(key).handle(request)
      }
    }
    throw new FileNotFoundException("No handler found for path "+request.location)
  }
}
