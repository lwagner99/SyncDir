package de.lukaswagner.syncdir.server

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  val config: Config = ConfigFactory.load("application.conf")
  val syncDir = new File(config.getString("de.lukaswagner.server.syncDir"))
}
