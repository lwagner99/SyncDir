package de.lukaswagner.syncdir.client

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}


object Config {
  val config: Config = ConfigFactory.load("application.conf")
  val syncDir = new File(config.getString("de.lukaswagner.client.syncDir"))
  val backupDir = new File(config.getString("de.lukaswagner.client.backupDir"))
  val historizedFileChecksumPath = config.getString("de.lukaswagner.client.historizedFileChecksumPath")
  val serverIp = config.getString("de.lukaswagner.client.serverIp")
  val serverPort = config.getString("de.lukaswagner.client.serverPort")
  val akkaUrl = s"$serverIp:$serverPort"
}
