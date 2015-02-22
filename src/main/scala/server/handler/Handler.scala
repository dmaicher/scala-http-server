package server.handler

import server.http.request.Request
import server.http.response.Response

trait Handler {
  def handle(request: Request): Response
}
