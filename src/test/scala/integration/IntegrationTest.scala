package integration

import backend.{FileUtil, OutputDirectory}
import backend.llvm.LlvmRunner
import compiler.{CheckMain, CheckReturns, Compiler, NormalizeQuadSubblocks, ParseClasses, TypePhase}
import org.scalatest.{FlatSpec, Matchers}
import parser.latte.LatteParser

class IntegrationTest extends FlatSpec with Matchers {
  behavior of "Integration test"

  def checkListEq[A](result: List[A], expected: List[A]): Unit = {
    assert(result.length == expected.length, s"wrong list length: $result")

    for (((resultElt, expectedElt), index) <- result.zip(expected).zipWithIndex) {
      if (resultElt != expectedElt) {
        fail(s"$resultElt != $expectedElt at line $index, $result")
      }
    }
  }

  for (fileWithResult <- FileEnumerator.getWithResult) {

    val llvmCompiler =
      Compiler
        .debug("parser", LatteParser)
        .nextStage("parse_class", ParseClasses)
        .nextStage("typed", TypePhase)
        .nextStage("check_main", CheckMain)
        .nextStage("check_returns", CheckReturns)
        .nextStage("untyped", compiler.UntypingPhase)
        .nextStage("quad", compiler.LatteToQuadCode)
        .nextStage("normalize_quad", NormalizeQuadSubblocks)

    val directory = OutputDirectory.createTemporary.withSourceFile(
      fileWithResult.filename, fileWithResult.fileContent)

    val result = fileWithResult.expectedResult

    it should s"work for file ${fileWithResult.sourceFile} in ${directory.directory}" in {
      val llvmCodeOrError = llvmCompiler compile directory


      llvmCodeOrError match {
        case Left(exceptions) => fileWithResult.expectedResult(Left(exceptions))
        case Right(llvmCode) => {
          val result = LlvmRunner.compile(llvmCode, directory)
          if (result.success) {
            val runResult = LlvmRunner.run(directory)
            if(runResult.success) {
              val output = FileUtil.parseOut(runResult.stdout)
              fileWithResult.expectedResult(Right(output))
            } else {
              fail("Runtime error:\n" + result.stderr)
            }
          } else {
            fail(result.stdout ++ result.stderr)
          }
        }
      }
    }
  }
}
