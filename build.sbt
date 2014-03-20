name := """akka-clustering"""

version := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % "2.2.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
  "com.typesafe.akka" %% "akka-kernel" % "2.2.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.1",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.fusesource" % "sigar" % "1.6.4",
  "com.amazonaws" % "aws-java-sdk" % "1.4.2.1",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test")