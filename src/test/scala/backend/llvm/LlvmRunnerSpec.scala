package backend.llvm

import backend.{FileMatchers, OutputDirectory}
import language.LLVM
import org.scalatest.{FlatSpec, Matchers}
import language.Type._

object Definitions {
  def validBlock: LLVM.Block = LLVM.Block(LLVM.FunctionId("main", IntType), List(), {
    import LLVM._

    Vector(Subblock(Vector(
      AssignOp(register("a", IntType), Add, 2, 3),
      PrintInt(register("a", IntType)),
      Return(LLVM.Value("0", IntType))
    )))
  })

  def validCode = LLVM.Code("", List(validBlock))
}

class LlvmRunnerSpec extends FlatSpec with Matchers with FileMatchers {
  import Definitions.validCode

  behavior of "LLVM Runner"

  it should "compileWithoutErrors" in {
    val outputDirectory = OutputDirectory.createTemporary

    val result = LlvmRunner.compile(validCode, outputDirectory)

    outputDirectory.llvmFile should be a nonemptyFile
    outputDirectory.llvmExecutable should be a nonemptyFile
    outputDirectory.llvmTempExecutable should not be nonemptyFile

    val output = LlvmRunner.run(outputDirectory)
    assert(output == List("5"))
  }

  it should "be able to just run" in {
    assert(LlvmRunner.runCode(validCode) == List("5"))
  }
}
