package com.limitra.sdk

import com.limitra.sdk.core.{definition => df}
import com.limitra.sdk.core.extension._
import com.limitra.sdk.core.provider._
import org.joda.time.DateTime

package object core {
  lazy val Crypto = new CryptoProvider

  def Reflect: ReflectProvider = new ReflectProvider
  def Config(configName: String) = new ConfigProvider(configName)

  def DateTime = new df.DateTime(None)

  implicit def LongExt(value: Long) = new LongExtender(None, value)

  implicit def BigDecimalExt(value: BigDecimal) = new BigDecimalExtender(None, value)

  implicit def StringExt(value: String) = new StringExtender(value)

  implicit def IntExt(value: Int) = new IntExtender(value)

  implicit def DateTimeExt(value: DateTime) = new DateTimeExtender(value)
}
