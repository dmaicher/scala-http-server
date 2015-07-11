package server.handler

import java.io._
import server.http.headers.Headers
import server.http.request.Request
import server.http.response.Response
import server.http.response.body.InputStreamResponseBody
import server.mime.MimeTypeRegistry
import server.utils.{DateUtils, FileUtils}

class StaticFileHandler(val mimeTypeRegistry: MimeTypeRegistry, val fileUtils: FileUtils, val dateUtils: DateUtils, val path: String) extends Handler {
  override def handle(request: Request): Option[Response] = {
    val file = new File(path+request.location)
    if(!file.exists()) {
      return None
    }

    val responseHeaders = new Headers()
    val lastModified = file.lastModified()
    var status = 200
    if(lastModified > 0) {
      responseHeaders += Headers.LAST_MODIFIED -> dateUtils.getHttpFormattedDate(lastModified)
      request.headers.get(Headers.IF_MODIFIED_SINCE).map(d => dateUtils.getUnixTsFromHttpFormattedDate(d.head)).foreach(_.foreach(ifModSince => {
        if(lastModified <= ifModSince) {
          status = 304
        }
      }))
    }

    val resp = new Response(
      status,
      new InputStreamResponseBody(new FileInputStream(file), Some(file.length())),
      responseHeaders
    )

    fileUtils.getExtension(file).foreach(ext =>
      mimeTypeRegistry.getMimeTypeByExtension(ext).foreach(m =>
        resp.setContentType(m)
      )
    )

    Some(resp)
  }
}
