import AssemblyKeys._

lazy val root = (project in file("."))
  .settings(assemblySettings)
  .settings(
    name := "codebrag-slack-bot",
    version := "1.0",
    scalaVersion := "2.11.6",
    mainClass in assembly := Some("play.core.server.NettyServer"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
  ).enablePlugins(PlayScala)

libraryDependencies += "com.cyberdolphins" %% "slime" % "0.1.9-SNAPSHOT" withSources() exclude("commons-logging", "commons-logging")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"