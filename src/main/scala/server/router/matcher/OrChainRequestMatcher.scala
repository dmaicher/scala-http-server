package server.router.matcher

import server.http.request.Request

class OrChainRequestMatcher(val matchers: RequestMatcher*) extends RequestMatcher {
  def matching(request: Request): Boolean = {
    matchers.find(_.matching(request)).nonEmpty
  }
}
