package jvm

import java.io._
import java.nio.file.Paths
import java.util.concurrent.{ScheduledThreadPoolExecutor, ThreadPoolExecutor}

import jvm.JasminRunner.{StdErr, StdOut}

import scala.util.Random

case class CommandResult(success: Boolean, stdout: StdOut, stderr: StdErr)

object CommandResult {
  def empty: CommandResult = CommandResult(false, "", "")
}

object OutputDirectory {
  def create(directory: File): OutputDirectory = {
    directory.mkdir()
    new OutputDirectory(directory)
  }
}

class OutputDirectory(val directory: File) {
  private def subfile(filename: String): File = {
    new File(Paths.get(directory.getCanonicalPath, filename).toString)
  }

  def sourceFile: File = subfile("source.j")
  def classFile: File = subfile("Main.class")

  def path: String = directory.getAbsolutePath

  def jarFile: File = {
    new File(directory.getCanonicalPath + ".jar")
  }
}

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.function.Consumer

private class StreamGobbler(val inputStream: InputStream,
                            val consumers: Consumer[String]*) extends Runnable {
  override def run(): Unit = {
    val singleConsumer = consumers.reduce((consumerA, consumerB) => consumerA.andThen(consumerB))

    new BufferedReader(new InputStreamReader(inputStream)).lines.forEach(singleConsumer)
  }
}

object JasminRunner {
  type StdOut = String
  type StdErr = String
  val rand = new Random

  def run(assemblyCode: String): String = {
    val directoryCounter = rand.nextInt()
    val outputDirectory = OutputDirectory.create(new File(s"/tmp/mrjp$directoryCounter"))

    val result = JasminRunner.compile(assemblyCode, outputDirectory)
    val output = JasminRunner.runJava(outputDirectory)
    output
  }

  // Simple interface to the run of jasmin.jar on a file
  // Saves to output file
  def compile(assemblyCode: String, outputDirectory: OutputDirectory): CommandResult = {
    System.err.print("Saving to file: " + outputDirectory.sourceFile)
    System.err.print(assemblyCode)

    val writer: BufferedWriter = new BufferedWriter(new FileWriter(outputDirectory.sourceFile))
    writer.write(assemblyCode)
    writer.close()

    runCommand(
      s"""
         |java
         | -jar /home/svp/Programming/mrjp/lib/jasmin.jar
         | -d ${outputDirectory.path}
         | ${outputDirectory.sourceFile}""".stripMargin.replaceAll("[\n]", ""))
  }

  def runJava(outputDirectory: OutputDirectory): String = {
    runCommand(
      s"java Main",
      Some(new File(outputDirectory.path))
    ).stdout
  }

  private def runCommand(javaCommand: String, maybeWorkingDir: Option[File] = None): CommandResult = {

    val process = maybeWorkingDir match {
      case None =>
        Runtime.getRuntime.exec(javaCommand)
      case Some(workingDir) =>
        Runtime.getRuntime.exec(javaCommand, Array[String](), workingDir)
    }

    val stdout = new StringBuilder

    val stdoutGobbler = new StreamGobbler(process.getInputStream,
      (input) => stdout ++= input)

    val errorGobbler = new StreamGobbler(process.getErrorStream,
      (input) => System.err.println("exec error: " + input))

    val executor = new ScheduledThreadPoolExecutor(2)

    executor.execute(stdoutGobbler)
    executor.submit(errorGobbler)
    val exitCode = process.waitFor
    executor.shutdown()

    CommandResult(exitCode == 0, stdout.toString, "")
  }
}