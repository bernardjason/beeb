name := """beeb"""
// Basic Enterprise Edition By Bernard

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))

scalaVersion := "2.11.7"

mainClass in Compile := Some("beeb.rpn.CommandLine")

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.ow2.asm" % "asm" % "6.0_ALPHA",
  "jline" % "jline" % "2.14.2",
  "com.typesafe.play" %% "play-json" % "2.5.4"
)

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "2.4.2",
  "com.h2database"  %  "h2"                % "1.4.192",
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
)


libraryDependencies ++= Seq(
  "org.scalatra"              %%  "scalatra"         % "2.3.0",
  "org.scalatra"              %%  "scalatra-json"    % "2.3.0",
  "org.json4s"                %%  "json4s-jackson"   % "3.2.9",
  "org.eclipse.jetty"         %   "jetty-webapp"     % "9.3.0.M1" ,
  "com.typesafe.play" %% "play-ws" % "2.5.4" % "provided"
)

libraryDependencies ++= Seq(
 "org.scalatest" %% "scalatest" % "2.1.7" % "test"
)


resolvers += Classpaths.typesafeResolver

