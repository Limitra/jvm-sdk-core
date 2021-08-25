name := "core"

version := "0.0.8"
scalaVersion := "2.13.6"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.scala-lang" % "scala-reflect" % "2.13.6",
  "joda-time" % "joda-time" % "2.10.10",
  "com.google.apis" % "google-api-services-oauth2" % "v2-rev157-1.25.0"
)
logLevel := Level.Error

pgpReadOnly := false
