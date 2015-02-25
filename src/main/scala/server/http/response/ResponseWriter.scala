package server.http.response

import java.io.OutputStream
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
      response.headers += "Content-Type" -> (response.contentType+"; charset="+encoding)
    }

    var bodyOutputStream = outputStream
    if(request.protocol == HttpProtocol.HTTP_1_1 && request.headers.getOrElse(Headers.TRANSFER_ENCODING, "").equals("chunked")) {
      response.headers += Headers.TRANSFER_ENCODING -> "chunked"
      bodyOutputStream = new ChunkedOutputStream(outputStream)
    }

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
