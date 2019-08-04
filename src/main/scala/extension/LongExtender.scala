package com.limitra.sdk.core.extension

import com.limitra.sdk.core._
import org.joda.time.{DateTime, DateTimeZone}

/**
  * Extension methods for Long data type.
  */
final class LongExtender(zone: Option[Int], value: Long) {
  private val _culture = Config("Application").Get("Culture")

  private def _zone(tag: Int): Int = {
    return zone.getOrElse(_culture.OptionInt("DefaultTimeZone").getOrElse(tag))
  }

  def ToDate(tag: Int = 0): DateTime = {
    val timezone = DateTimeZone.forOffsetHours(_zone(tag))
    return new DateTime(value, timezone)
  }
}
