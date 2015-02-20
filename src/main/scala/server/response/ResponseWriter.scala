package server.response

import java.io.{DataOutputStream, OutputStream}

class ResponseWriter {
  private val lb = "\n"
  private val encoding = "UTF-8"

  def write(outputStream: OutputStream, response: Response, protocol: String, writeBody: Boolean): Unit = {
    val out = new DataOutputStream(outputStream)
    out.write((protocol+" "+response.status+" OK"+lb).getBytes(encoding))

    response.headers += "Connection" -> "close"

    if(!response.body.isEmpty && response.contentType != null && !response.contentType.isEmpty) {
      response.headers += "Content-Type" -> (response.contentType+"; charset="+encoding)
    }

    for((k,v) <- response.headers) {
      out.write((k+": "+v+lb).getBytes(encoding))
    }

    out.write(lb.getBytes(encoding))

    if(writeBody && !response.body.isEmpty) {
      out.write(response.body.getBytes(encoding))
    }

    out.flush()
    out.close()
  }
}
