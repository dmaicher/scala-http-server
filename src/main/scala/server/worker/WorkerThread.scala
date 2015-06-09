package server.worker

import server.http.request.parser.RequestParser
import server.http.response.ResponseWriter
import server.router.Router

class WorkerThread(
    private val runnable: Runnable,
    private val requestParser: RequestParser,
    private val responseWriter: ResponseWriter,
    private val router: Router
  ) extends Thread(runnable) {
  def getRequestParser = requestParser
  def getResponseWriter = responseWriter
  def getRouter = router
}
