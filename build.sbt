lazy val root = (project in file(".")).
  settings(
    name := "codebrag-slack-bot",
    version := "1.0",
    scalaVersion := "2.11.6"
  ).enablePlugins(PlayScala)

libraryDependencies += "com.cyberdolphins" %% "slime" % "0.1.9-SNAPSHOT" withSources()

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"