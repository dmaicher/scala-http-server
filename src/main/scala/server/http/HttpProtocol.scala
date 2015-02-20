package server.http

object HttpProtocol {
  val HTTP_1 = "HTTP/1.0"
  val HTTP_1_1 = "HTTP/1.1"

  private val values = List(
    HTTP_1, HTTP_1_1
  )

  def exists(name: String): Boolean = {
    values.contains(name)
  }
}