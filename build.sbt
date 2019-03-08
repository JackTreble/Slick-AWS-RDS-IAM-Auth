name := "slick-aws-rds-iam-auth"

version := "0.1"

scalaVersion := "2.12.8"

enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.506"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
