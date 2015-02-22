package server.http.request.parser

class ParseRequestException(val reason: String = "") extends Exception(reason)

