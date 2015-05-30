package server.handler.FastCgi

import java.io.ByteArrayOutputStream

class NameValuePairList() {
  private var items = List[NameValuePair]()
  def add(item: NameValuePair): Unit = {
    items = items :+ item
  }
  def add(name: String, value: String): Unit = {
    add(new NameValuePair(name, value))
  }
   def toByteArray: Array[Byte] = {
     val out = new ByteArrayOutputStream()
     items.foreach(i => out.write(i.toByteArray))
     out.toByteArray
   }
 }
