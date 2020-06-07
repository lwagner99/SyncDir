package de.lukaswagner.syncdir.client

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}


object Config {
  val config: Config = ConfigFactory.load("application.conf")
  val syncDir = new File(config.getString("de.lukaswagner.client.syncDir"))
  val backupDir = new File(config.getString("de.lukaswagner.client.backupDir"))
  val historizedFileChecksumPath = config.getString("de.lukaswagner.client.historizedFileChecksumPath")
  val serverIp = "127.0.0.1"
  val serverPort = "50000"
  val akkaUrl = s"$serverIp:$serverPort"
}
