package server.http.headers

class HeaderParser {
  def parse(s: String): Headers  = {
    val headers = new Headers
    s.trim.split("\r\n(?!( |\t))").map(_.split(":", 2).map(_.trim)).foreach(h => {
      if(h.length == 2) {
        headers += h(0) -> (headers.get(h(0)).map(_+",").getOrElse("")+h(1).replaceAll("\r\n[ \t]+", " "))
      }
    })
    headers
  }
}
