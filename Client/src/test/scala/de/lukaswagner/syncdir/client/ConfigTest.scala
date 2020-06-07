package de.lukaswagner.syncdir.client

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

class ConfigTest extends FlatSpec with LazyLogging {

  "ConfigSession" should "work" in {
    logger.info(Config.akkaUrl)
  }

}
