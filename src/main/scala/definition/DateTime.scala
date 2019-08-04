package com.limitra.sdk.core.definition

import com.limitra.sdk.core._

class DateTime(zone: Option[Int]) {
  private val _culture = Config("Application").Get("Culture")

  private def _zone(tag: Int): org.joda.time.DateTimeZone = {
    return org.joda.time.DateTimeZone.forOffsetHours(zone.getOrElse(_culture.OptionInt("DefaultTimeZone").getOrElse(tag)))
  }

  def Now(tag: Int = 0): org.joda.time.DateTime = {
    return org.joda.time.DateTime.now(_zone(tag))
  }
}
