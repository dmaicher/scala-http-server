package server.http.encoding

import java.io.{InputStream, FilterInputStream}

class ChunkedInputStream(in: InputStream) extends FilterInputStream(in) {
  //TODO: implement ;P
}
