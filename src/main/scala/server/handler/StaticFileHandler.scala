package server.handler

import java.io._
import server.http.request.Request
import server.http.response.Response
import server.http.response.body.InputStreamResponseBody
import server.mime.MimeTypeRegistry
import server.utils.FileUtils

class StaticFileHandler(val mimeTypeRegistry: MimeTypeRegistry, fileUtils: FileUtils, val path: String) extends Handler {
  override def handle(request: Request): Response = {
    val file = new File(path+"/"+request.location)
    if(!file.exists()) {
      throw new FileNotFoundException(path+"/"+request.location)
    }

    val resp = new Response(200, new InputStreamResponseBody(new FileInputStream(file), Some(file.length())))

    fileUtils.getExtension(file).foreach(ext =>
      mimeTypeRegistry.getMimeTypeByExtension(ext).foreach(m =>
        resp.setContentType(m)
      )
    )

    resp
  }
}
