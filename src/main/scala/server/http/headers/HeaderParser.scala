package server.http.headers

class HeaderParser {
  def parse(s: String): Headers  = {
    val headers = new Headers
    s.trim.split("\r\n(?!( |\t))").map(_.split(":", 2).map(_.trim)).foreach(h => {
      if(h.length == 2) {
        headers.putWithAppend(h(0), h(1).replaceAll("\r\n[ \t]+", " "))
      }
    })
    headers
  }
}
