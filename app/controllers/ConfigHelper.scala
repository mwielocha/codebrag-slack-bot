package controllers

import com.cyberdolphins.slime.common.Channel
import com.cyberdolphins.slime.outgoing.Outbound
import play.api.Configuration
import scala.collection.JavaConversions._

/**
  * Created by mwielocha on 22/02/16.
  */
trait ConfigHelper {

  val config: Configuration

  lazy val repoRouting: Map[Channel, List[String]] = {

    config.getConfig("slack.channels").map { config =>

        config.keys.map {

          channel =>
            Channel(channel) -> config
              .getStringList(channel)
              .map(_.toList)
              .getOrElse(Nil)

        }.toMap

    }.getOrElse(Map.empty)

  }

  def findRoutes(repoName: String): Iterable[Channel] = {
    repoRouting.filter {
      case (channel, patterns) =>
        patterns.exists(repoName.matches)
    }.keys
  }

  def route(repoName: String)(func: Channel => Outbound): List[Outbound]= {
    findRoutes(repoName).toList.map(func)
  }
}
