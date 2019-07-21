package extension

import java.text._
import java.util.Locale

import com.limitra.sdk.core._

/**
  * Extension methods for Double data type.
  */
final class DoubleExtender(value: Double) {
  private val _culture = Config("Culture")

  def ToFixed(frac: Int = 2): Double = {
    var pattern = ""
    for (i <- 1 to frac) {
      pattern += "#"
    }
    val format = new DecimalFormat("###." + pattern)
    return Double(format.format(value))
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
