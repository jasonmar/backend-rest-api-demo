name := "ai-cal"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.7"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.7"

// Amazon
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.136"

// Google
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"
libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0"
libraryDependencies += "com.google.apis" % "google-api-services-calendar" % "v3-rev249-1.22.0"

// Twilio
libraryDependencies += "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0"

// Microsoft
resolvers += Resolver.bintrayRepo("msopentech", "Maven")
libraryDependencies += "com.microsoft.services" % "live-auth" % "0.13.0"
libraryDependencies += "com.microsoft.services" % "outlook-services-java" % "1.0.0"
libraryDependencies += "com.microsoft.services" % "graph-services-java" % "0.2.5"

// Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

mainClass in (Compile, run) := Some("app.Main")