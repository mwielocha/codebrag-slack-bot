package controllers

import javax.inject.Singleton

import akka.actor.{Props, ActorSystem}
import com.cyberdolphins.slime.OutgoingOnlySlackBotActor
import com.cyberdolphins.slime.SlackBotActor.{SlackBotConfig, Connect}
import com.cyberdolphins.slime.outgoing.ComplexOutboundMessage
import com.google.inject.Inject
import com.typesafe.config.Config
import model._
import play.api.{Configuration, Logger}
import play.api.mvc.{Action, Controller}
import scala.collection.JavaConversions._
import com.cyberdolphins.slime.common._

import scala.concurrent.ExecutionContext

/**
  * Created by mikwie on 12/02/16.
  */

@Singleton
class Application @Inject()(private val system: ActorSystem,
                            private val config: Configuration)
                           (implicit private val ec: ExecutionContext)
  extends Controller {

  private val slackToken = config.getString("slack.token")
    .getOrElse(throw new RuntimeException("slack.token configuration key missing"))

  private val channels = config.getStringList("slack.channels")
    .getOrElse(throw new RuntimeException("slack.channels configuration key missing"))
    .map(Channel(_))

  private val codebragUrl = config.getString("codebrag.url")
    .getOrElse(throw new RuntimeException("codebrag.url configuration key missing"))

  private val logger = Logger(getClass)

  private val formatMessage: PartialFunction[(Channel, Event), ComplexOutboundMessage] = {

    case (channel, e: CommitsLoadedEvent) if e.newCommits.nonEmpty =>

      ComplexOutboundMessage(
        s"[*${e.repoName}*] New commit(s) to review:", channel,
        e.newCommits.map {
          case Commit(sha, message, author, _, _) =>

            // <http://www.foo.com|www.foo.com>

            val link = s"<$codebragUrl/#/${e.repoName}/commits/$sha|${sha.take(7)}>"

            Attachment(s"$link: ${message.trim} - by *$author*")
              .withMarkdownIn(MarkdownInValues.text)
              .withColor(Color.warning)
        }: _*)
  }

  private val slack = system.actorOf(Props[OutgoingOnlySlackBotActor])

  slack ! Connect(slackToken, SlackBotConfig(autoEscape = false))

  def hook = Action(parse.json) { request =>

      logger.debug(s"Action: ${request.body}")

      val event = request.body.as[Event]

      channels.foreach {
        case channel if formatMessage.isDefinedAt(channel, event)
          => slack ! formatMessage(channel, event)
        case _ => //ignore
      }

    Ok
  }
}
