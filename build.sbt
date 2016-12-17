name := "backend-rest-api-demo"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe.akka" % "akka-http_2.12" % "10.0.0"

libraryDependencies += "com.typesafe.akka" % "akka-http-spray-json_2.12" % "10.0.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.67"

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test"

mainClass in (Compile, run) := Some("app.Main")
