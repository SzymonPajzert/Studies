package backend.jvm

import backend.{BackendRunner, CommandResult, FileUtil, OutputDirectory}

object JasminRunner extends BackendRunner[String] {
  // Simple interface to the run of jasmin.jar on a file
  // Saves to output file
  def compile(assemblyCode: String, outputDirectory: OutputDirectory): CommandResult[String] = {
    FileUtil.saveToFile(assemblyCode, outputDirectory.jasminFile)

    FileUtil.runCommand(
      s"""
         |java
         | -jar /home/svp/Programming/mrjp/deps/jasmin.jar
         | -d ${outputDirectory.path}
         | ${outputDirectory.jasminFile}""".stripMargin.replaceAll("[\n]", ""),
      outputDirectory)
  }

  def run(outputDirectory: OutputDirectory): List[String] = {
    val commandResult = FileUtil.runCommand(
      s"java Main",
      outputDirectory,
      Some(outputDirectory.path)
    )

    commandResult map FileUtil.parseOut stdout
  }
}