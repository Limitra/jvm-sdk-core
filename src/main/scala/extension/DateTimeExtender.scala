package com.limitra.sdk.core.extension

import java.util.Locale

import com.limitra.sdk.core.Config
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat

/**
  * Extension methods for DateTime data type.
  */
final class DateTimeExtender(lang: Option[String], value: DateTime) {
  private val _alternate = "yyyy-MM-dd'T'HH:mm:ssZ"
  private val _culture = Config("Application").Get("Culture")
  private val _defaultLang = _culture.OptionString("DefaultLang").getOrElse("en-US")
  private val _defaultTimeZone = _culture.OptionInt("DefaultTimeZone").getOrElse(0)

  private def _datePattern(ptrn: String): String = {
    if (ptrn != null) ptrn else _culture.OptionString("DefaultDatePattern").getOrElse(_alternate)
  }

  private def _timePattern(ptrn: String): String = {
    if (ptrn != null) ptrn else _culture.OptionString("DefaultTimePattern").getOrElse(_alternate)
  }

  private def _dateTimePattern(ptrn: String): String = {
    if (ptrn != null) ptrn else _culture.OptionString("DefaultDateTimePattern").getOrElse(_alternate)
  }

  def ToDateText(pattern: String = null): String = {
    DateTimeFormat.forPattern(_datePattern(pattern))
      .withZone(DateTimeZone.forOffsetHours(_defaultTimeZone))
      .withLocale(Locale.forLanguageTag(_defaultLang)).print(value)
  }

  def ToTimeText(pattern: String = null): String = {
    DateTimeFormat.forPattern(_timePattern(pattern))
      .withZone(DateTimeZone.forOffsetHours(_defaultTimeZone))
      .withLocale(Locale.forLanguageTag(_defaultLang)).print(value)
  }

  def ToDateTimeText(pattern: String = null): String = {
    DateTimeFormat.forPattern(_dateTimePattern(pattern))
      .withZone(DateTimeZone.forOffsetHours(_defaultTimeZone))
      .withLocale(Locale.forLanguageTag(_defaultLang)).print(value)
  }
}
