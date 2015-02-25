package server.http.response

import java.io.{DataOutputStream, OutputStream}

class ResponseWriter {
  private val lb = "\n"
  private val encoding = "UTF-8"

  def write(outputStream: OutputStream, response: Response, protocol: String, writeBody: Boolean): Unit = {

    val stringBuilder = new StringBuilder

    //TODO: get correct status message
    stringBuilder.append(protocol+" "+response.status+" OK"+lb)

    response.headers += "Connection" -> "close"
    if(!response.body.isEmpty && response.contentType != null && !response.contentType.isEmpty) {
      response.headers += "Content-Type" -> (response.contentType+"; charset="+encoding)
    }

    for((k,v) <- response.headers) {
      stringBuilder.append(k+": "+v+lb)
    }

    stringBuilder.append(lb)

    if(writeBody && !response.body.isEmpty) {
      stringBuilder.append(response.body)
    }

    val out = new DataOutputStream(outputStream)
    out.write(stringBuilder.toString().getBytes(encoding))
    out.flush()
  }
}
