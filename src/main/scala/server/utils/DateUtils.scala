package server.utils

import java.text.{ParseException, SimpleDateFormat}
import java.util.{TimeZone, Locale, Calendar}

class DateUtils {
  private val httpDateFormat = "EEE, dd MMM yyyy HH:mm:ss z"
  def getHttpFormattedDate(unixTs: Long): String = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(unixTs)
    val dateFormat = new SimpleDateFormat(httpDateFormat, Locale.US)
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    dateFormat.format(calendar.getTime)
  }

  def getUnixTsFromHttpFormattedDate(date: String): Option[Long] = {
    try {
      Some(new SimpleDateFormat(httpDateFormat, Locale.US).parse(date).getTime)
    }
    catch {
      case e: ParseException => None
    }
  }
}
