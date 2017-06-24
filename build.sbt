name := "ai-cal"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.2"

resolvers += Resolver.bintrayRepo("msopentech", "Maven")

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.7"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.7"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.136"

// Google
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"

libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0"

libraryDependencies += "com.google.apis" % "google-api-services-calendar" % "v3-rev249-1.22.0"


// Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

mainClass in (Compile, run) := Some("app.Main")