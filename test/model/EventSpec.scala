package model

import org.scalatest._
import play.api.libs.json.Json

/**
  * Created by mwielocha on 12/02/16.
  */
class EventSpec extends FlatSpec with Matchers {

  "Event" should "parse itself from json" in {

    val json =
      """
        |{
        |	"commitInfo": {
        |		"repoName": "a-repo",
        |		"sha": "475a9911ef2e9795d42842df2097de41d07580c5",
        |		"message": "release-4.7.5-SNAPSHOT\n",
        |		"authorName": "John Doe",
        |		"authorEmail": "john.doe@mail.com",
        |		"committerName": "John Doe",
        |		"committerEmail": "john.doe@mail.com",
        |		"authorDate": "2016-01-28T14:28:09Z",
        |		"commitDate": "2016-01-28T14:28:09Z",
        |		"parents": ["c9d5337ad38fe088b47e53d14531cdc84c1f9deb"]
        |	},
        |	"likedBy": {
        |		"name": "jim.beam",
        |		"emailLowerCase": "jim.beam@mail.com",
        |		"aliases": {
        |			"emailAliases": []
        |		}
        |	},
        |	"like": {
        |		"postingTime": "2016-02-12T20:30:57Z"
        |	},
        |	"hookName": "like-hook",
        |	"hookDate": "2016-02-12T20:30:57Z"
        |}
      """.stripMargin

    Json.parse(json).as[LikeEvent].commitInfo.sha shouldBe "475a9911ef2e9795d42842df2097de41d07580c5"

  }
}
