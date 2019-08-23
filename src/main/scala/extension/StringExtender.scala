package com.limitra.sdk.core.extension

import com.limitra.sdk.core._
import scala.util.Try

/**
  * Extension methods for String data type.
  */
final class StringExtender(value: String) {
  def ToDecimal(frac: Int = 2): BigDecimal = {
    var vrVal: String = value
    if(vrVal.contains(".") && vrVal.contains(",")) {
      if(vrVal.indexOf(",") < vrVal.indexOf("."))
        vrVal = vrVal.replace(",", "")
      else
        vrVal = vrVal.replace(".", "")
    }
    val convert = Try(BigDecimal(vrVal.replace(",", ".")))
    if (convert.isSuccess) return convert.get.ToFixed(frac) else BigDecimal(0)
  }

  def ToUrl: Option[String] = {
    val urls = this.ToUrlSeq
    return if(urls.length > 0) urls.headOption else None
  }

  def ToUrlSeq: Seq[String] = {
    val num = """(?<url>(http:|https:|ftp:[/][/]|www.)([a-z]|[A-Z]|[0-9]|[-?%&=/.]|[~])*)""".r
    return num.findAllIn(value).toList
  }
}
