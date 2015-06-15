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

class Headers extends HashMap[HeaderKey, List[String]] {
  def put(key: String, values: List[String]): Option[List[String]] = put(new HeaderKey(key), values)
  def put(key: String, value: String): Option[List[String]] = put(new HeaderKey(key), value)
  def put(key: HeaderKey, value: String): Option[List[String]] = put(key, List(value))

  def putWithAppend(key: String, value: String): Option[List[String]] = putWithAppend(new HeaderKey(key), value)
  def putWithAppend(key: HeaderKey, value: String): Option[List[String]] = get(key) match {
    case None => put(key, List(value))
    case Some(l) => put(key, l :+ value)
  }

  def get(key: String): Option[List[String]] = get(new HeaderKey(key))

  def getOrElse[B1 >: List[String]](key: String, default: => B1): B1 = getOrElse(new HeaderKey(key), default)

  def contains(key: String): Boolean = contains(new HeaderKey(key))
  def containsWithValue(key: String, value: String): Boolean = containsWithValue(new HeaderKey(key), value)
  def containsWithValue(key: HeaderKey, value: String): Boolean = get(key) match {
    case None => false
    case Some(v) => v.map(_.toLowerCase).contains(value)
  }

  def remove(key: String): Option[List[String]] = remove(new HeaderKey(key))

  def +=(kv: (String, String)): Headers = {
    put(kv._1, kv._2)
    this
  }

  def +=(kv: (HeaderKey, String))(implicit d: DummyImplicit): Headers = {
    put(kv._1, kv._2)
    this
  }

  def +=(kv: (String, List[String]))(implicit d1: DummyImplicit, d2: DummyImplicit): Headers = {
    put(new HeaderKey(kv._1), kv._2)
    this
  }

  def foldValues = map(kv => {
    kv._1 -> kv._2.mkString(",")
  })

  def foldValuesExceptKey(key: HeaderKey) = {
    map(kv => {
      kv._1 -> {
        if(key.equals(kv._1)) kv._2 else List(kv._2.mkString(","))
      }
    })
  }
}

object Headers {
  val CONTENT_LENGTH = new HeaderKey("Content-Length")
  val CONTENT_TYPE = new HeaderKey("Content-Type")
  val CONTENT_ENCODING = new HeaderKey("Content-Encoding")
  val TRANSFER_ENCODING = new HeaderKey("Transfer-Encoding")
  val ACCEPT_ENCODING = new HeaderKey("Accept-Encoding")
  val CONNECTION = new HeaderKey("Connection")
  val KEEP_ALIVE = new HeaderKey("Keep-Alive")
  val SET_COOKIE = new HeaderKey("Set-Cookie")
  val HOST = new HeaderKey("Host")
}