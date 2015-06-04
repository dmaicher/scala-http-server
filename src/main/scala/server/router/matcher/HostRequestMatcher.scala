package server.router.matcher

import server.http.request.Request

class HostRequestMatcher(val host: String) extends RequestMatcher {
  def matching(request: Request): Boolean = {
    request.host.forall(_.equals(host))
  }
}
