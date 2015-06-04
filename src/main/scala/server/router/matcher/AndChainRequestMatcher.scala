package server.router.matcher

import server.http.request.Request

class AndChainRequestMatcher(val matchers: RequestMatcher*) extends RequestMatcher {
  def matching(request: Request): Boolean = {
    matchers.forall(_.matching(request))
  }
}
