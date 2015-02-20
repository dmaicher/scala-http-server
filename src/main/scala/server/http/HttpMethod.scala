package server.http

object HttpMethod {
  val GET = "GET"
  val POST = "POST"
  val HEAD = "HEAD"
  val PUT = "PUT"
  val DELETE = "DELETE"
  val TRACE = "TRACE"
  val OPTIONS = "OPTIONS"
  val CONNECT = "CONNECT"

  private val values = List(
    GET, POST, HEAD, PUT, DELETE, TRACE, OPTIONS, CONNECT
  )

  def exists(name: String): Boolean = {
    values.contains(name)
  }
}
