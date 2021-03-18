name := "core"

version := "0.0.6"
scalaVersion := "2.13.5"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.scala-lang" % "scala-reflect" % "2.13.5",
  "joda-time" % "joda-time" % "2.10.10",
  "com.google.apis" % "google-api-services-oauth2" % "v2-rev157-1.25.0"
)

pgpReadOnly := false
