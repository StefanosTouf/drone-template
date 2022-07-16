import sbt.project

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.12.16"

lazy val root = (project in file("."))
  .settings(
    name                                   := "drone-sample-project",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"
  )
