package server.handler

import server.request.Request
import server.response.Response

trait Handler {
  def handle(request: Request): Response
}
