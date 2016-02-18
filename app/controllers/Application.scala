package controllers

import javax.inject.Singleton

import akka.actor.{Props, ActorSystem}
import com.cyberdolphins.slime.OutgoingOnlySlackBotActor
import com.cyberdolphins.slime.SlackBotActor.{SlackBotConfig, Connect}
import com.cyberdolphins.slime.outgoing.{SimpleOutboundMessage, Outbound, ComplexOutboundMessage}
import com.google.inject.Inject
import com.typesafe.config.Config
import model._
import org.joda.time.DateTime
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

  private def linkToCommit(repoName: String, sha: String): String = {
    s"<$codebragUrl/#/$repoName/commits/$sha|${sha.take(7)}>"
  }

  private def linkToCommit(repoName: String, commit: Commit): String = {
    linkToCommit(repoName, commit.sha)
  }

  private def linkToCommit(commit: CommitInfo): String = {
    linkToCommit(commit.repoName, commit.sha)
  }

  private val formatMessage: PartialFunction[(Channel, Event), Outbound] = {

    case (channel, CommitsLoadedEvent(repoName, currentSHA, newCommits, hookName, hookDate)) if newCommits.nonEmpty =>

      val title = s"[*$repoName*] New commit(s) to review:"

      val attachments = newCommits.map {
        case Commit(sha, message, author, _, _) =>

          // <http://www.foo.com|www.foo.com>

          val link = linkToCommit(repoName, sha)

          Attachment(s"$link: ${message.trim} - by *$author*")
            .withMarkdownIn(MarkdownInValues.text)
            .withColor(Color.warning)
      }

      ComplexOutboundMessage(
        title, channel,
        attachments: _*)


    case (channel, CommentAddedEvent(commitInfo, commentedBy, comment, hookName, hookDate)) =>

      val link = linkToCommit(commitInfo)

      val title = s"[*${commitInfo.repoName}*] New comment to *${commitInfo.authorName}'s* commit:"

      val attachment = Attachment(s"$link: ${comment.message.trim} - by *${commentedBy.name}*")
        .withMarkdownIn(MarkdownInValues.text)
        .withColor(Color.warning)

      ComplexOutboundMessage(title, channel, attachment)

    case (channel, CommitReviewedEvent(commitInfo, reviewedBy, hookName, hookDate)) =>

      val link = linkToCommit(commitInfo)

      val title = s"[*${commitInfo.repoName}*] My name is *${reviewedBy.name}* and I approve this commit:"

      val attachment = Attachment(s"$link: ${commitInfo.message.trim} - by *${commitInfo.authorName}*")
        .withMarkdownIn(MarkdownInValues.text)
        .withColor(Color.good)

      ComplexOutboundMessage(title, channel, attachment)
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
