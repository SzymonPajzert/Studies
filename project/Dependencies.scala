import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val mockito = "org.mockito" % "mockito-core" % "1.8.5" // % Test
  lazy val javaCup = "java_cup.runtime" % "java_cup-runtime" % "11.0" from "file:///home/svp/Programming/mrjp/lib/java-cup-11b-runtime.jar"
}
