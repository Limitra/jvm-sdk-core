package com.limitra.sdk.core.extension

import java.text._
import java.util.Locale

import com.limitra.sdk.core._

import scala.math._

/**
  * Extension methods for BigDecimal data type.
  */
final class BigDecimalExtender(value: BigDecimal) {
  private val _culture = Config("Culture")

  def ToFixed(frac: Int = 2): BigDecimal = {
    var pattern = ""
    for (i <- 1 to frac) {
      pattern += "#"
    }
    val format = new DecimalFormat("###." + pattern)
    return BigDecimal(format.format(value))
  }

  def ToText(frac: Int = 2, tag: String = "en-US"): String = {
    val locale = Locale.forLanguageTag(_culture.OptionString("Lang").getOrElse(tag))
    val format = NumberFormat.getNumberInstance(locale)
    format.setMaximumFractionDigits(frac)
    format.setMinimumFractionDigits(frac)
    return format.format(value)
  }

  def ToMoney(tag: String = "en-US"): String = {
    val locale = Locale.forLanguageTag(_culture.OptionString("Lang").getOrElse(tag))
    val format = NumberFormat.getCurrencyInstance(locale)
    var result = format.format(value)
    if (tag == "tr-TR") {
      result = result.replace("TL", "₺")
    }
    return result
  }
}
