import org.joda.time.DateTime
import play.api.libs.json._

/**
  * Created by mikwie on 12/02/16.
  */
package object model {

  sealed trait Event

  /*
  "commitInfo": {
    "repoName": string,
    "sha": string,
    "message": string,
    "authorName": string,
    "authorEmail": string,
    "committerName": string,
    "committerEmail": string,
    "authorDate": date,
    "commitDate": date,
    "parents": [string, string]
  }
   */

  case class CommitInfo(repoName: String, sha: String,
                        message: String, authorName: String,
                        authorEmail: String, committerName: String,
                        committerEmail: String, authorDate: DateTime,
                        commitDate: DateTime, parents: List[String])

  /*
  {
    "commitInfo": {
      "repoName": string,
      "sha": string,
      "message": string,
      "authorName": string,
      "authorEmail": string,
      "committerName": string,
      "committerEmail": string,
      "authorDate": date,
      "commitDate": date,
      "parents": [string, string]
    },
    "likedBy": {
      "name": string,
      "emailLowerCase": string,
      "aliases": [{
        "alias": string
      }]
    },
    "like": {
      "postingTime": date,
      "fileName": string,
      "lineNumber": int
    },
    "hookName": "like-hook",
    "hookDate": date
  }
   */

  case class Like(postingTime: DateTime,
                  fileName: Option[String],
                  lineNumber: Option[Int])

  case class Alias(alias: String)

  case class By(name: String, emailLowerCase: String/*, aliases: List[Alias]*/)

  case class LikeEvent(commitInfo: CommitInfo,
                       likedBy: By,
                       like: Like,
                       hookName: String,
                       hookDate: DateTime) extends Event

  /*
  {
    "commitInfo": {
      "repoName": string,
      "sha": string,
      "message": string,
      "authorName": string,
      "authorEmail": string,
      "committerName": string,
      "committerEmail": string,
      "authorDate": date,
      "commitDate": date,
      "parents": [string, string]
    },
    "unlikedBy": {
      "name": string,
      "emailLowerCase": string,
      "aliases": [{
        "alias": string
      }]
    },
    "like": {
      "postingTime": date,
      "fileName": string,
      "lineNumber": int
    },
    "hookName": "unlike-hook",
    "hookDate": date
  }
   */

  case class UnlikeEvent(commitInfo: CommitInfo, unlikedBy: By,
                         like: Like, hookName: String,
                         hookDate: DateTime) extends Event

  /*
  {
    "commitInfo": {
      "repoName": string,
      "sha": string,
      "message": string,
      "authorName": string,
      "authorEmail": string,
      "committerName": string,
      "committerEmail": string,
      "authorDate": date,
      "commitDate": date,
      "parents": [string, string]
    },
    "commentedBy": {
      "name": string,
      "emailLowerCase": string,
      "aliases": [{
        "alias": string
      }]
    },
    "comment": {
      "message": string,
      "postingTime": date,
      "fileName": string,
      "lineNumber": int
    },
    "hookName": "comment-added-hook",
    "hookDate": date
  }
   */

  case class Comment(message: String,
                     postingTime: DateTime,
                     fileName: String,
                     lineNumber: Option[Int])

  case class CommentAddedEvent(commitInfo: CommitInfo,
                               commentedBy: By,
                               comment: Comment,
                               hookName: String,
                               hookDate: DateTime) extends Event

  /*

  {
    "commitInfo": {
      "repoName": string,
      "sha": string,
      "message": string,
      "authorName": string,
      "authorEmail": string,
      "committerName": string,
      "committerEmail": string,
      "authorDate": date,
      "commitDate": date,
      "parents": [string, string]
    },
    "reviewedBy": {
      "name": string,
      "emailLowerCase": string,
      "aliases": [{
        "alias": string
      }]
    },
    "hookName": "commit-reviewed-hook",
    "hookDate": date
  }
   */

  case class CommitReviewedEvent(commitInfo: CommitInfo,
                                 reviewedBy: By,
                                 hookName: String,
                                 hookDate: DateTime) extends Event

  /*
  {
    "repoName": string,
    "currentSHA": string,
    "newCommits": [
      {
        "sha": string,
        "message": string,
        "authorName": string,
        "authorEmail": string,
        "date": date
      }
    ],
    "hookName": "new-commits-loaded-hook",
    "hookDate": date
  }
   */

  case class Commit(sha: String,
                    message: String,
                    authorName: String,
                    authorEmail: String,
                    date: DateTime)

  case class CommitsLoadedEvent(repoName: String,
                                currentSHA: String,
                                newCommits: List[Commit],
                                hookName: String,
                                hookDate: DateTime) extends Event

  /*

  {
    "newUser": {
      "name": string,
      "emailLowerCase": string,
      "aliases": [{
        "alias": string
      }]
    },
    "login": string,
    "fullName": string,
    "hookName": "new-user-registered-hook",
    "hookDate": date
  }
   */

  case class NewUserRegisteredEvent(newUser: By,
                                    login: String,
                                    fullName: String,
                                    hookName: String,
                                    hookDate: DateTime) extends Event

  object Event {

    implicit val DateTimeReads = new Reads[DateTime] {
      override def reads(json: JsValue): JsResult[DateTime] = {
        JsSuccess(DateTime.parse(json.as[String]))
      }
    }

    private val hookName = "hookName"

    private val newCommitsLoaded  = "new-commits-loaded-hook"
    private val newUserRegistered = "new-user-registered-hook"
    private val commitReviewed    = "commit-reviewed-hook"
    private val commentAdded      = "comment-added-hook"
    private val unlike            = "unlike-hook"
    private val like              = "like-hook"

    implicit val CommitInfoFormat = Json.format[CommitInfo]
    implicit val LikeFormat = Json.format[Like]
    implicit val AliasFormat = Json.format[Alias]
    implicit val ByFormat = Json.format[By]
    implicit val CommentFormat = Json.format[Comment]
    implicit val CommitFormat = Json.format[Commit]

    implicit val CommitsLoadedEventFormat = Json.format[CommitsLoadedEvent]
    implicit val NewUserRegisteredEventFormat = Json.format[NewUserRegisteredEvent]
    implicit val CommitReviewedEventFormat = Json.format[CommitReviewedEvent]
    implicit val CommentAddedEventFormat = Json.format[CommentAddedEvent]
    implicit val LikeEventFormat = Json.format[LikeEvent]
    implicit val UnlikeEventFormat = Json.format[UnlikeEvent]

    implicit val EventReads = new Reads[Event] {
      override def reads(json: JsValue): JsResult[Event] = {
        (json \ hookName).as[String] match {
          case `newUserRegistered` => NewUserRegisteredEventFormat.reads(json)
          case `newCommitsLoaded` => CommitsLoadedEventFormat.reads(json)
          case `commitReviewed` => CommitReviewedEventFormat.reads(json)
          case `commentAdded` => CommentAddedEventFormat.reads(json)
          case `unlike` => UnlikeEventFormat.reads(json)
          case `like` => LikeEventFormat.reads(json)
        }
      }
    }
  }
}
