package com.limitra.sdk.core.extension

import com.limitra.sdk.core._
import org.joda.time.{DateTime, DateTimeZone}

/**
  * Extension methods for Long data type.
  */
final class LongExtender(value: Long) {
  private val _culture = Config("Culture")

  def ToDate(zone: String = "UTC"): DateTime = {
    val timezone = DateTimeZone.forID(_culture.OptionString("Time").getOrElse(zone))
    return new DateTime(value, timezone)
  }
}
