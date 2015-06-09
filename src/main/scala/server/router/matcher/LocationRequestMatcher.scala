package server.router.matcher

import server.http.request.Request
import scala.util.matching.Regex

class LocationRequestMatcher(val regex: Regex) extends RequestMatcher {
  def matching(request: Request): Boolean = {
    regex.findFirstIn(request.location).nonEmpty
  }
}
