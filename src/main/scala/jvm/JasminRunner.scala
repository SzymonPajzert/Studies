package jvm

import java.io._

import jvm.JasminRunner.{StdErr, StdOut}

case class CommandResult(success: Boolean, stdout: StdOut, stderr: StdErr)

object CommandResult {
  def empty: CommandResult = CommandResult(false, "", "")
}

object JasminRunner {
  type StdOut = String
  type StdErr = String


  // Simple interface to the run of jasmin.jar on a file
  // Saves to output file
  def compile(assemblyCode: String, outputFile: File): CommandResult = {
    CommandResult.empty
  }

  def runJava(javaFile: File): CommandResult = {
    val rt = Runtime.getRuntime();
    val pr = rt.exec("java -jar map.jar time.rel test.txt debug");
    CommandResult.empty
  }
}