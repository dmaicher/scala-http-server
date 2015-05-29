package server.handler

import java.nio.charset.{CodingErrorAction, Charset}

import server.http.request.Request
import server.http.response.Response

class StaticFileHandler(val path: String) extends Handler {
  private val decoder = Charset.forName("UTF-8").newDecoder()
  decoder.onMalformedInput(CodingErrorAction.IGNORE)
  override def handle(request: Request): Response = {
    val source = scala.io.Source.fromFile(path+"/"+request.location)(decoder)
    val content = source.mkString
    source.close()
    new Response(200, content)
  }
}
