package com.limitra.sdk.core.extension

import com.limitra.sdk.core._
import org.joda.time.{DateTime, DateTimeZone}

/**
  * Extension methods for Long data type.
  */
final class LongExtender(lang: Option[String], value: Long) {
  private val _culture = Config("Application").Get("Culture")

  private def _zone(zone: String): String = {
    return lang.getOrElse(_culture.OptionString("DefaultTimeZone").getOrElse(zone))
  }

  def ToDate(zone: String = "UTC"): DateTime = {
    val timezone = DateTimeZone.forID(_zone(zone))
    return new DateTime(value, timezone)
  }
}
