package integration

import backend.OutputDirectory
import backend.llvm.LlvmRunner
import compiler.Compiler
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
      val llvmCodeOrError = llvmCompiler compile directory

      llvmCodeOrError match {
        case Left(exceptions) => assert(Left(exceptions) == fileWithResult.expectedResult.get)
        case Right(llvmCode) => {
          val result = LlvmRunner.compile(llvmCode, directory)
          if(result.success) {
            println(directory.directory)
            assert(Right(LlvmRunner.run(directory)) == fileWithResult.expectedResult.get)
          } else {
            fail(result.stdout ++ result.stderr)
          }
        }
      }
    }
  }
}
