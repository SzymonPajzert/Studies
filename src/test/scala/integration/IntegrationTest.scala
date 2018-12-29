package integration

import compiler.Compiler
import backend.OutputDirectory
import backend.llvm.LlvmRunner
import org.scalatest.{FlatSpec, Matchers}
import parser.latte.LatteParser

class IntegrationTest extends FlatSpec with Matchers {
  behavior of "Integration test"

  for (fileWithResult <- FileEnumerator.getWithResult) {

    val llvmCompiler =
      Compiler
        .debug("parser", LatteParser)
        .nextStage("staticAnalysis", compiler.LatteStaticAnalysis)
        .nextStage("quad", compiler.LatteToQuadCode)

    val directory = OutputDirectory.createTemporary.withSourceFile(
      fileWithResult.filename, fileWithResult.fileContent)

    println(directory.directory)

    it should s"return good result in LLVM for file ${fileWithResult.filename} in ${directory.directory}" in {
      assert((for {
        llvmCode <- llvmCompiler compile directory
        _ = LlvmRunner.compile(llvmCode, directory)
      } yield LlvmRunner.run(directory)) === fileWithResult.expectedResult)
    }
  }
}
