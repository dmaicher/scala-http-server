package server.http.headers

import scala.collection.mutable.HashMap

class HeaderKey(private val key: String) {
  protected lazy val keyLower = if(key != null) key.toLowerCase else null

  override def equals(other: scala.Any): Boolean = {
    if(other == null || !other.isInstanceOf[HeaderKey]) {
      return false
    }

    val otherKeyLower = other.asInstanceOf[HeaderKey].keyLower

    if(keyLower == null) {
      return otherKeyLower == null
    }

    keyLower.equals(otherKeyLower)
  }

  override def hashCode(): Int = {
    if(keyLower != null) keyLower.hashCode() else 0
  }

  override def toString: String = key
}

class Headers extends HashMap[HeaderKey, String] {
  def put(key: String, value: String): Option[String] = put(new HeaderKey(key), value)

  def get(key: String): Option[String] = get(new HeaderKey(key))

  def getOrElse[B1 >: String](key: String, default: => B1): B1 = getOrElse(new HeaderKey(key), default)

  def contains(key: String): Boolean = contains(new HeaderKey(key))
  def containsWithValue(key: String, value: String): Boolean = containsWithValue(new HeaderKey(key), value)
  def containsWithValue(key: HeaderKey, value: String): Boolean = get(key) match {
    case None => false
    case Some(v) => v.equalsIgnoreCase(value)
  }

  def +=(kv: (String, String)): Headers = +=(new HeaderKey(kv._1) -> kv._2)
}

object Headers {
  val CONTENT_LENGTH = new HeaderKey("Content-Length")
  val CONTENT_TYPE = new HeaderKey("Content-Type")
  val TRANSFER_ENCODING = new HeaderKey("Transfer-Encoding")
  val ACCEPT_ENCODING = new HeaderKey("Accept-Encoding")
  val CONNECTION = new HeaderKey("Connection")
  val KEEP_ALIVE = new HeaderKey("Keep-Alive")
}