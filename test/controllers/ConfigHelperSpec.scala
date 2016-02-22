package controllers

import com.cyberdolphins.slime.common.Channel
import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.{Matchers, FlatSpec}
import play.api.Configuration

/**
  * Created by mwielocha on 22/02/16.
  */
class ConfigHelperSpec extends FlatSpec with Matchers {

  "ConfigHelper" should "load repo routes config" in {

    val backend = Channel("ygo-backend")
    val frontend = Channel("ygo-frontend")

    object TestConfigHelper extends ConfigHelper {
      val config = Configuration(ConfigFactory.load())
    }

    TestConfigHelper.findRoutes("opi-service") should contain(backend)
    TestConfigHelper.findRoutes("opi_front") should contain(frontend)
    TestConfigHelper.findRoutes("opigram-library") should contain(backend)
    TestConfigHelper.findRoutes("entities") should contain(backend)
    TestConfigHelper.findRoutes("entities") === Seq(backend)
  }
}
