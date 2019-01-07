package integration

import backend.OutputDirectory
import backend.llvm.LlvmRunner
import compiler.{Compiler, TypePhase}
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
        .nextStage("typer", TypePhase)
        .nextStage("staticAnalysis", compiler.UntypingPhase)
        .nextStage("quad", compiler.LatteToQuadCode)

    val directory = OutputDirectory.createTemporary.withSourceFile(
      fileWithResult.filename, fileWithResult.fileContent)

    val result = fileWithResult.expectedResult

    if (result.isDefined) {
      it should s"return good result in LLVM for file ${fileWithResult.filename} in ${directory.directory}" in {
        val llvmCodeOrError = llvmCompiler compile directory


        llvmCodeOrError match {
          case Left(exceptions) if exceptions.forall(_.isInstanceOf[ParseError]) => fail(exceptions.toString)
          case Left(exceptions) => assert(Left(exceptions) == result.get)
          case Right(llvmCode) => {
            val result = LlvmRunner.compile(llvmCode, directory)
            if (result.success) {

              checkListEq(LlvmRunner.run(directory), fileWithResult.expectedResult.get.right.get)
            } else {
              fail(result.stdout ++ result.stderr)
            }
          }
        }
      }
    }
  }
}
