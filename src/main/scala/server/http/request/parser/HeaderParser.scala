package server.http.request.parser

import server.http.Headers

class HeaderParser {
  def parse(s: String): Headers  = {
    val headers = new Headers
    s.trim.split("\r\n(?!( |\t))").map(l => l.split(":", 2).map(_.trim)).foreach(h => {
      if(h.size == 2) {
        headers += h(0) -> (headers.get(h(0)).map(_+",").getOrElse("")+h(1).replaceAll("\r\n[ \t]+", " "))
      }
    })
    headers
  }
}
