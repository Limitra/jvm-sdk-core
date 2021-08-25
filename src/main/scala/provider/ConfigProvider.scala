package com.limitra.sdk.core.provider

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.Duration

/**
  * It is used to make customized reading from the configuration file.
  */
sealed class ConfigProvider(configName: String, config: Option[Config] = None) {
  private val _config = if (config.isDefined) config.get else ConfigFactory.load().getConfig(configName)

  def Get(subConfig: String): ConfigProvider = {
    var newConfig: Option[Config] = None
    if(_config.isResolved && !_config.isEmpty && !_config.getIsNull(subConfig))
      newConfig = Some(_config.getConfig(subConfig))

    return new ConfigProvider("", newConfig)
  }

  def String(key: String, default: String = ""): String = {
    return if (_config.hasPath(key)) _config.getString(key) else default
  }

  def Int(key: String, default: Int = 0): Int = {
    return if (_config.hasPath(key)) _config.getInt(key) else default
  }

  def Long(key: String, default: Long = 0): Long = {
    return if (_config.hasPath(key)) _config.getLong(key) else default
  }

  def Double(key: String, default: Double = 0): Double = {
    return if (_config.hasPath(key)) _config.getDouble(key) else default
  }

  def Boolean(key: String, default: Boolean = false): Boolean = {
    return if (_config.hasPath(key)) _config.getBoolean(key) else default
  }

  def Duration(key: String, default: Duration): Duration = {
    return if (_config.hasPath(key)) _config.getDuration(key).asInstanceOf[Duration] else default
  }

  def OptionString(key: String): Option[String] = {
    return if (_config.hasPath(key)) Some(_config.getString(key)) else None
  }

  def OptionInt(key: String, default: Int = 0): Option[Int] = {
    return if (_config.hasPath(key)) Some(_config.getInt(key)) else None
  }

  def OptionLong(key: String, default: Long = 0): Option[Long] = {
    return if (_config.hasPath(key)) Some(_config.getLong(key)) else None
  }

  def OptionDouble(key: String, default: Double = 0): Option[Double] = {
    return if (_config.hasPath(key)) Some(_config.getDouble(key)) else None
  }

  def OptionBoolean(key: String, default: Boolean = false): Option[Boolean] = {
    return if (_config.hasPath(key)) Some(_config.getBoolean(key)) else None
  }

  def OptionDuration(key: String, default: Duration): Option[Duration] = {
    return if (_config.hasPath(key)) Some(_config.getDuration(key).asInstanceOf[Duration]) else None
  }
}
