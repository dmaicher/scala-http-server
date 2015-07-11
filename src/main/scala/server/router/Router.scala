package server.router

import java.io.FileNotFoundException
import server.handler.Handler
import server.http.request.Request
import server.http.response.Response
import server.router.matcher.RequestMatcher

import scala.collection.mutable

class Router extends Handler {
  private val handlers = new mutable.LinkedHashMap[RequestMatcher, Handler]()

  def registerHandler(handler: Handler, requestMatcher: RequestMatcher): Unit = {
    handlers.put(requestMatcher, handler)
  }

  override def handle(request: Request): Option[Response] = {
    handlers.find(_._1.matching(request)).map(_._2) match {
      case Some(h) => h.handle(request)
      case _ => None
    }
  }
}
