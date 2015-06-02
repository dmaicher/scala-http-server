package server.handler

import java.io._
import server.http.request.Request
import server.http.response.Response
import server.http.response.body.InputStreamResponseBody

class StaticFileHandler(val path: String) extends Handler {
  override def handle(request: Request): Response = {
    val file = new File(path+"/"+request.location)
    if(!file.exists()) {
      throw new FileNotFoundException(path+"/"+request.location)
    }
    new Response(200, new InputStreamResponseBody(new FileInputStream(file), Some(file.length())))
  }
}
