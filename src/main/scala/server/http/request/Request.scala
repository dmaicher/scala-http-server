package server.http.request

import server.http.Headers

class Request(val method: String, val location: String, val protocol: String, val headers: Headers, val body: String = "")
