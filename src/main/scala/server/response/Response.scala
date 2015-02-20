package server.response

import server.http.Headers

class Response(val status: Int, val body: String = "", val contentType: String = "text/html", val headers: Headers = new Headers)
