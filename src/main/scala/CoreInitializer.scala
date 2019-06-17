package com.limitra.sdk

import com.limitra.sdk.core.extension._
import com.limitra.sdk.core.provider._
import org.joda.time.DateTime

package object core {
  lazy val Reflect = new ReflectProvider
  lazy val Crypto = new CryptoProvider
  def Config(configName: String) = new ConfigProvider(configName)

  implicit def StringExt(value: String) = new StringExtender(value)

  implicit def BigDecimalExt(value: BigDecimal) = new BigDecimalExtender(value)

  implicit def IntExt(value: Int) = new IntExtender(value)

  implicit def LongExt(value: Long) = new LongExtender(value)

  implicit def DateTimeExt(value: DateTime) = new DateTimeExtender(value)
}
