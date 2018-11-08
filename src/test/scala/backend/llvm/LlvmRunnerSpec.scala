package backend.llvm

import backend.{FileMatchers, OutputDirectory}
import org.scalatest.{FlatSpec, Matchers}

object Definitions {
  def validCode: LLVM.Code = {
    import LLVM._

    List(
      AssignOp(Register("a", IntType), Add, 2, 3),
      PrintInt(Register("a", IntType))
    )
  }
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
    assert(output == List(5))
  }

  it should "be able to just run" in {
    assert(LlvmRunner.runCode(validCode) == List(5))
  }
}
