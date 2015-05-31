package server.http.headers

import scala.collection.mutable.HashMap

//TODO: has to be case insensitive!
class Headers extends HashMap[String, String]
object Headers {
  val CONTENT_LENGTH = "Content-Length"
  val TRANSFER_ENCODING = "Transfer-Encoding"
  val ACCEPT_ENCODING = "Accept-Encoding"
  val CONNECTION = "Connection"
}

/*
class HeaderName(name: String) {
  override def hashCode(): Int = name.toLowerCase.hashCode

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: HeaderName
  }
}
*/