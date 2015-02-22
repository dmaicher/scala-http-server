name := "scala-http-server"

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.10",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
)