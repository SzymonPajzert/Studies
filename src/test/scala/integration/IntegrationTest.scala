package integration

import backend.{FileUtil, OutputDirectory}
import backend.llvm.LlvmRunner
import compiler.{Compiler, ParseClasses, TypePhase}
import org.scalatest.{FlatSpec, Matchers}
import parser.ParseError
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
        .nextStage("untyped", compiler.UntypingPhase)
        .nextStage("quad", compiler.LatteToQuadCode)

    val directory = OutputDirectory.createTemporary.withSourceFile(
      fileWithResult.filename, fileWithResult.fileContent)

    val result = fileWithResult.expectedResult

    if (result.isDefined) {
      it should s"return good result in LLVM for file ${fileWithResult.filename} in ${directory.directory}" in {
        val llvmCodeOrError = llvmCompiler compile directory


        llvmCodeOrError match {
          case Left(exceptions) => assert(Left(exceptions) == result.get)
          case Right(llvmCode) => {
            val result = LlvmRunner.compile(llvmCode, directory)
            if (result.success) {
              val runResult = LlvmRunner.run(directory)
              if(runResult.success) {
                val output = FileUtil.parseOut(runResult.stdout)
                val expected = fileWithResult.expectedResult.get.right.get
                checkListEq(output, expected)
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
}
