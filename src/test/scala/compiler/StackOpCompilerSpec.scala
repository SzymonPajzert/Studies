package compiler

import backend.{FileUtil, OutputDirectory}
import org.scalatest.{FlatSpec, Matchers}
import parser.instant.{FileEnumerator, InstantParser}

class StackOpCompilerSpec extends FlatSpec with Matchers {
  behavior of "Stack of compiler spec"

  for (fileWithResult <- FileEnumerator.getWithResult) {
    val filename = fileWithResult.filename
    it should s"calculate safe stack size for: $filename" in {
      val fileContent = FileUtil.readTestFile(filename)
      val directory = OutputDirectory.createTemporary.withSourceFile(filename, fileContent)

      val Right(stackOps) = (compiler.withParser(InstantParser) ~> InstantToStackOp) compile directory

      assert(stackOps.stackSize >= fileWithResult.stackDepth)
    }
  }


}
