package server.router.matcher

import server.http.request.Request

trait RequestMatcher {
  def matching(request: Request): Boolean
}
