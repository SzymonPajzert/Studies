// reviewed: 2018.12.29

package backend.llvm

import backend.{BackendRunner, CommandResult, FileUtil, OutputDirectory}
import language.LLVM

import scala.language.{implicitConversions, postfixOps}

object LlvmRunner extends BackendRunner[LLVM.Code] {
  override def compile(code: LLVM.Code, outputDirectory: OutputDirectory): CommandResult[String] = {
    implicit val runner = FileUtil.commandRunner
    import FileUtil.Interactive._

    saveToFile(LLVM.serializeCode(code), outputDirectory.llvmFile)

    runCommand(
      s"""
         |llvm-as
         | -o ${outputDirectory.llvmTempExecutable}
         | ${outputDirectory.llvmFile}""".stripMargin.replaceAll("[\n]", ""),
      outputDirectory)

    runCommand(
      s"""
         |llvm-link
         | -o ${outputDirectory.llvmExecutable}
         | ${outputDirectory.llvmTempExecutable} ${LLVM.runtimeLocation}
       """.stripMargin,
      outputDirectory
    )

    runCommand(s"rm ${outputDirectory.llvmTempExecutable}", outputDirectory)

    runner.finish
  }

  override def run(outputDirectory: OutputDirectory): List[String] = {
    FileUtil.runCommand(s"lli ${outputDirectory.llvmExecutable}", outputDirectory) map FileUtil.parseOut stdout
  }
}
