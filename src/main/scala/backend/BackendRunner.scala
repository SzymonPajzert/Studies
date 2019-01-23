// reviewed: 2018.12.29

package backend

import java.io.File

import scala.util.Random

case class CommandResult[T](success: Boolean, stdout: T, stderr: String) {
  def map[B](f: T => B): CommandResult[B] = this.copy(stdout = f(stdout))
}

object CommandResult {
  def empty: CommandResult[String] = CommandResult[String](success = false, "", "")
}


trait BackendRunner[Code] {
  val rand = new Random

  def runCode(assemblyCode: Code): CommandResult[String] = {
    val directoryCounter = rand.nextInt()
    val outputDirectory = OutputDirectory.create(new File(s"/tmp/mrjp$directoryCounter"))

    val result = compile(assemblyCode, outputDirectory)
    val output = run(outputDirectory, "")
    output
  }

  def compile(code: Code, outputDirectory: OutputDirectory): CommandResult[String]
  def run(outputDirectory: OutputDirectory, input: String): CommandResult[String]
}
