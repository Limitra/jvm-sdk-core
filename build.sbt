name := "core"

version := "0.0.4"
scalaVersion := "2.12.9"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.scala-lang" % "scala-reflect" % "2.12.9",
  "joda-time" % "joda-time" % "2.3",
  "com.google.apis" % "google-api-services-oauth2" % "v1-rev134-1.22.0"
)

pgpReadOnly := false
