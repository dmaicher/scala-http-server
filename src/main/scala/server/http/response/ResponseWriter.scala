package server.http.response

import java.io._
import java.util.zip.GZIPOutputStream
import server.http.encoding.ChunkedOutputStream
import server.http.headers.Headers
import server.http.{HttpProtocol, HttpMethod}
import server.http.request.Request

class ResponseWriter {
  private val CRLF = "\r\n"
  private val encoding = "UTF-8"

  def write(outputStream: OutputStream, request: Request, response: Response): Unit = {

    val stringBuilder = new StringBuilder

    //TODO: get correct status message
    stringBuilder.append(request.protocol+" "+response.status+" OK"+CRLF)

    //TODO: also make sure we close it if we don't have a content length and no chunks
    response.headers += "Connection" -> {
      if(request.keepAlive) {
        "Keep-Alive"
      }
      else {
        "Close"
      }
    }

    var bodyOutputStream = outputStream

    //TODO: make configurable if chunked should be used [+ do not use for HEAD requests or 304 response]
    if(response.hasBody && response.body.getLength.isDefined) {
      response.headers += Headers.CONTENT_LENGTH -> response.body.getLength.get.toString
    }
    else if( request.protocol == HttpProtocol.HTTP_1_1) {
      response.headers += Headers.TRANSFER_ENCODING -> "chunked"
      bodyOutputStream = new ChunkedOutputStream(bodyOutputStream, 2048)
    }

    /*
    //TODO: make work ;P
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

    if(request.method != HttpMethod.HEAD && response.hasBody) {
      response.writeBody(bodyOutputStream)
    }

    bodyOutputStream match {
      case c: ChunkedOutputStream => c.finish()
      case _ =>
    }

    bodyOutputStream.flush()
  }
}
