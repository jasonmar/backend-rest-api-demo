name := "backend-rest-api-demo"

version := "1.1"

scalaVersion := "2.12.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.7"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.7"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.136"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

mainClass in (Compile, run) := Some("app.Main")
