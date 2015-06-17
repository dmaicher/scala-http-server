package server.utils

import org.scalamock.scalatest._
import org.scalatest._

class DateUtilsTestSpec  extends FlatSpec with Matchers with MockFactory {
  "getHttpFormattedDate" should "return correct date" in {
    val du = new DateUtils

    du.getHttpFormattedDate(0) should be("Thu, 01 Jan 1970 00:00:00 GMT")
    du.getHttpFormattedDate(1434566563000L) should be("Wed, 17 Jun 2015 18:42:43 GMT")
  }

  "getUnixTsFromHttpFormattedDate" should "return correct ts" in {
    val du = new DateUtils

    du.getUnixTsFromHttpFormattedDate("invalid date str") should be(None)
    du.getUnixTsFromHttpFormattedDate("Thu, 01 Jan 1970 00:00:00 GMT") should be(Some(0))
    du.getUnixTsFromHttpFormattedDate("Wed, 17 Jun 2015 18:42:43 GMT") should be(Some(1434566563000L))
  }
}