import sbt._
import Keys._
name := "typeclassess"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.0"
)

