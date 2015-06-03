package server.handler

import org.scalamock.scalatest._
import org.scalatest._

class StaticFileHandlerTestSpec  extends FlatSpec with Matchers with MockFactory {
  "StaticFileHandler" should "throw not found exception when file does not exist" in {
    /*
    intercept[FileNotFoundException] {
      new StaticFileHandler()
    }
    */
  }
}