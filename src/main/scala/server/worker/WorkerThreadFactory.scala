package server.worker

import java.util.concurrent.ThreadFactory
import server.http.connection.KeepAlivePolicy
import server.http.headers.HeaderParser
import server.http.request.parser.{RequestLineParser, RequestParser}
import server.http.response.ResponseWriter
import server.router.Router
import server.utils.DateUtils

class WorkerThreadFactory(private val router: Router, private val keepAlivePolicy: KeepAlivePolicy) extends ThreadFactory {
  override def newThread(runnable: Runnable): Thread = {
    val t = new WorkerThread(
      runnable,
      new RequestParser(new RequestLineParser, new HeaderParser),
      new ResponseWriter(keepAlivePolicy, new DateUtils),
      router
    )
    t.setDaemon(true)
    t
  }
}
