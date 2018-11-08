package backend

import java.io.File

import scala.util.Random

case class CommandResult[T](success: Boolean, stdout: T, stderr: String)

object CommandResult {
  def empty: CommandResult[String] = CommandResult[String](success = false, "", "")
}


trait BackendRunner[Code] {
  val rand = new Random

  def runCode(assemblyCode: Code): List[Int] = {
    val directoryCounter = rand.nextInt()
    val outputDirectory = OutputDirectory.create(new File(s"/tmp/mrjp$directoryCounter"))

    val result = compile(assemblyCode, outputDirectory)
    val output = run(outputDirectory)
    output
  }

  def compile(code: Code, outputDirectory: OutputDirectory): CommandResult[String]
  def run(outputDirectory: OutputDirectory): List[Int]
}
