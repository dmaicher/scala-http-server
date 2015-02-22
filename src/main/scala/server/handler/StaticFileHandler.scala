package server.handler

import server.http.request.Request
import server.http.response.Response

class StaticFileHandler(val path: String) extends Handler {
  override def handle(request: Request): Response = {
    val source = scala.io.Source.fromFile(path+"/"+request.location)
    val content = source.mkString
    source.close()
    new Response(200, content)
  }
}
