package server.config

class ServerConfig {
  var port = 80
  var maxWorkers = 50
  var workerShutdownTimeout = 60
  var allowHttpKeepAlive = true
  var httpKeepAliveTimeout = 5
  var httpKeepAliveMaxConnections = 100
}
