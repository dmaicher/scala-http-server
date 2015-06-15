package server.http.response

import java.io._
import java.util.zip.GZIPOutputStream
import server.http.connection.KeepAlivePolicy
import server.http.encoding.ChunkedOutputStream
import server.http.headers.Headers
import server.http.request.Request
import server.http.{HttpMethod, HttpProtocol, HttpStatus}

class ResponseWriter(private val keepAlivePolicy: KeepAlivePolicy) {
  private val CRLF = "\r\n"
  private val encoding = "UTF-8"
  private val keepAliveHeader = "timeout=%d, max=%d".format(keepAlivePolicy.timeout, keepAlivePolicy.max)

  def write(outputStream: OutputStream, request: Request, response: Response, requestCountForCurrentConnection: Int): Boolean = {

    val stringBuilder = new StringBuilder

    stringBuilder.append(
      request.protocol+" "+response.status+" "+HttpStatus.getPhrase(response.status).getOrElse("")+CRLF
    )

    val writeBody = response.hasBody && bodyAllowed(request, response)
    var keepAlive = this.keepAlive(request, requestCountForCurrentConnection)
    var bodyOutputStream = outputStream
    var chunkedOutputStream: Option[ChunkedOutputStream] = None
    var gzipOutputStream: Option[GZIPOutputStream] = None

    if(writeBody) {
      val acceptChunked = request.protocol == HttpProtocol.HTTP_1_1
      val acceptGzip = acceptChunked && request.headers.getOrElse(Headers.ACCEPT_ENCODING, List()).map(_.toLowerCase).mkString("").contains("gzip")
      val hasContentLength = response.body.getLength.isDefined
      if(acceptChunked && (!hasContentLength || acceptGzip)) {
        response.headers += Headers.TRANSFER_ENCODING -> "chunked"
        chunkedOutputStream = Some(new ChunkedOutputStream(bodyOutputStream, 2048))
        bodyOutputStream = chunkedOutputStream.get

        if(acceptGzip) {
          response.headers.remove(Headers.CONTENT_LENGTH)
          response.headers += Headers.CONTENT_ENCODING -> "gzip"
          gzipOutputStream = Some(new GZIPOutputStream(bodyOutputStream))
          bodyOutputStream = gzipOutputStream.get
        }
      }
      else if(hasContentLength) {
        response.headers += Headers.CONTENT_LENGTH -> response.body.getLength.get.toString
      }
      else {
        keepAlive = false
      }
    }
    else if(response.status == 304) {
      response.headers.remove(Headers.CONTENT_LENGTH)
    }

    if(keepAlive) {
      response.headers += Headers.CONNECTION -> "Keep-Alive"
      response.headers += Headers.KEEP_ALIVE -> keepAliveHeader
    }
    else {
      response.headers += Headers.CONNECTION -> "Close"
    }

    //http://tools.ietf.org/html/rfc6265#section-4.1
    response.headers.foldValuesExceptKey(Headers.SET_COOKIE).foreach(kv => {
      kv._2.foreach(v => {
        stringBuilder.append(kv._1+": "+v+CRLF)
      })
    })

    stringBuilder.append(CRLF)
    outputStream.write(stringBuilder.toString().getBytes(encoding))

    if(writeBody) {
      response.writeBody(bodyOutputStream)
    }

    gzipOutputStream.foreach(_.finish())
    chunkedOutputStream.foreach(_.finish())

    bodyOutputStream.flush()

    keepAlive
  }

  private def bodyAllowed(request: Request, response: Response): Boolean = {
    request.method != HttpMethod.HEAD && response.status != 304
  }

  private def keepAlive(request: Request, requestCountForCurrentConnection: Int): Boolean = {
    keepAlivePolicy.allow && requestCountForCurrentConnection < keepAlivePolicy.max && request.keepAlive
  }
}
