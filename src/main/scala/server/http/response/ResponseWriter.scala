package server.http.response

import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import server.http.{HttpProtocol, HttpMethod, Headers}
import server.http.encoding.ChunkedOutputStream
import server.http.request.Request

class ResponseWriter {
  private val CRLF = "\r\n"
  private val encoding = "UTF-8"

  def write(outputStream: OutputStream, request: Request, response: Response): Unit = {

    val stringBuilder = new StringBuilder

    //TODO: get correct status message
    stringBuilder.append(request.protocol+" "+response.status+" OK"+CRLF)

    response.headers += "Connection" -> "close"
    if(!response.body.isEmpty && response.contentType != null && !response.contentType.isEmpty) {
      //TODO: charset only for textual and not binary data (json, xml, svg, ...)
      response.headers += "Content-Type" -> (response.contentType+(if(response.contentType.startsWith("text/")) "; chartset="+encoding))
    }

    var bodyOutputStream = outputStream

    //TODO: make configurable if chunked should be used
    if(request.protocol == HttpProtocol.HTTP_1_1) {
      response.headers += Headers.TRANSFER_ENCODING -> "chunked"
      bodyOutputStream = new ChunkedOutputStream(bodyOutputStream, 100)
    }

    /*
    //TODO: consider priorities
    if(request.headers.getOrElse(Headers.ACCEPT_ENCODING, "").contains("gzip")) {
      response.headers += "Content-Encoding" -> "gzip"
      bodyOutputStream = new GZIPOutputStream(bodyOutputStream)
    }
    */

    for((k,v) <- response.headers) {
      stringBuilder.append(k+": "+v+CRLF)
    }

    stringBuilder.append(CRLF)
    outputStream.write(stringBuilder.toString().getBytes(encoding))

    if(request.method != HttpMethod.HEAD && !response.body.isEmpty) {
      bodyOutputStream.write(response.body.getBytes(encoding))
      bodyOutputStream.flush()
    }

    outputStream.flush()
  }
}
