package integration

import backend.OutputDirectory
import backend.llvm.LlvmRunner
import org.scalatest.{FlatSpec, Matchers}
import parser.latte.{FileEnumerator, LatteParser}

class IntegrationTest extends FlatSpec with Matchers {
  behavior of "Integration test"

  for (fileWithResult <- FileEnumerator.getWithResult) {

    val llvmCompiler =
      compiler.withParser(LatteParser) ~> compiler.LatteToQuadCode

    val directory = OutputDirectory.createTemporary.withSourceFile(
      fileWithResult.filename, fileWithResult.fileContent)

    println(directory.directory)

    it should s"return good result in LLVM for file ${fileWithResult.filename} in ${directory.directory}" in {
      val Right(llvmCode) = llvmCompiler compile directory

      LlvmRunner.compile(llvmCode, directory)
      assert(LlvmRunner.run(directory) === fileWithResult.expectedResult)
    }
  }
}
