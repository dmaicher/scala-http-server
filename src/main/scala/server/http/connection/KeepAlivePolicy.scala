package server.http.connection

case class KeepAlivePolicy(allow: Boolean, timeout: Int, max: Int)
