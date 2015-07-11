package server.handler

import server.http.request.Request
import server.http.response.Response

class ChainHandler(val handlers: Handler*) extends Handler {
  override def handle(request: Request): Option[Response] = {
    for(h <- handlers) {
      h.handle(request) match {
        case Some(r) => return Some(r)
        case _ =>
      }
    }
    None
  }
}
