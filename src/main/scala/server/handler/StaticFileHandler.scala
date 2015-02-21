package server.handler

import server.request.Request
import server.response.Response

class StaticFileHandler(val path: String) extends Handler {
  override def handle(request: Request): Response = {
    val source = scala.io.Source.fromFile(path+"/"+request.location)
    val content = source.mkString
    source.close()
    new Response(200, content)
  }
}
