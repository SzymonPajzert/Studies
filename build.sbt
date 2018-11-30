import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "mrjp",
    libraryDependencies ++= Seq(scalaTest % Test, mockito % Test, scalaz)
  )


mainClass in assembly := Some("Main")
