package backend.llvm

import backend.{BackendRunner, CommandResult, FileUtil, OutputDirectory}
import language.LLVM

import scala.language.{implicitConversions, postfixOps}

object LlvmRunner extends BackendRunner[LLVM.Code] {
  override def compile(code: LLVM.Code, outputDirectory: OutputDirectory): CommandResult[String] = {
    FileUtil.saveToFile(LLVM.serializeCode(code), outputDirectory.llvmFile)

    FileUtil.runCommand(
      s"""
         |llvm-as
         | -o ${outputDirectory.llvmTempExecutable}
         | ${outputDirectory.llvmFile}""".stripMargin.replaceAll("[\n]", ""),
      outputDirectory)

    FileUtil.runCommand(
      s"""
         |llvm-link
         | -o ${outputDirectory.llvmExecutable}
         | ${outputDirectory.llvmTempExecutable} ${LLVM.runtimeLocation}
       """.stripMargin,
      outputDirectory
    )

    FileUtil.runCommand(s"rm ${outputDirectory.llvmTempExecutable}", outputDirectory)
  }

  override def run(outputDirectory: OutputDirectory): List[String] = {
    FileUtil.runCommand(s"lli ${outputDirectory.llvmExecutable}", outputDirectory) map FileUtil.parseOut stdout
  }
}
