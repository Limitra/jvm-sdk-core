package extension

import java.text._
import java.util.Locale

import com.limitra.sdk.core._

/**
  * Extension methods for Double data type.
  */
final class DoubleExtender(lang: Option[String], value: Double) {
  private val _alternate = "en-US"
  private val _culture = Config("Application").Get("Culture")

  private def _lang(tag: String): String = {
    return lang.getOrElse(_culture.OptionString("DefaultLang").getOrElse(tag))
  }

  def ToFixed(frac: Int = 2): Double = {
    var pattern = ""
    for (i <- 1 to frac) {
      pattern += "#"
    }
    val format = new DecimalFormat("###." + pattern)
    return format.format(value).toDouble
  }

  def ToText(frac: Int = 2, tag: String = _alternate): String = {
    val locale = Locale.forLanguageTag(_lang(tag))
    val format = NumberFormat.getNumberInstance(locale)
    format.setMaximumFractionDigits(frac)
    format.setMinimumFractionDigits(frac)
    return format.format(value)
  }

  def ToMoney(tag: String = _alternate): String = {
    val lang = _lang(tag)
    val locale = Locale.forLanguageTag(lang)
    val format = NumberFormat.getCurrencyInstance(locale)
    var result = format.format(value)
    if (lang == "tr-TR") {
      result = result.replace("TL", "₺")
    }
    return result
  }
}
