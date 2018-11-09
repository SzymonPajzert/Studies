package integration

import backend.jvm.{JVMOp, JasminRunner}
import backend.llvm.LlvmRunner
import backend.{FileUtil, OutputDirectory}
import org.scalatest.{FlatSpec, Matchers}
import parser.instant.FileEnumerator

class ItegrationTest extends FlatSpec with Matchers {
  behavior of "Integration test"

  for (fileWithResult <- FileEnumerator.getWithResult) {
    val filename = fileWithResult.filename

    val jvmCompiler =
      compiler.DirectoryToInstant ~>
        compiler.InstantToStackOp ~>
        compiler.StackOpToJVM

    val llvmCompiler =
      compiler.DirectoryToInstant ~> compiler.InstantToLlvm

    val fileContent = FileUtil.readTestFile(filename)
    val directory = OutputDirectory.createTemporary.withSourceFile(filename, fileContent)

    it should s"return good result in Java for file $filename" in {
      val validJasminCode = JVMOp.createMain("Main", jvmCompiler compile directory)

      try {
        JasminRunner.compile(validJasminCode, directory)
        assert(JasminRunner.run(directory) == fileWithResult.expectedResult)
      } catch {
        case exception: Exception => {
          println(validJasminCode)
          throw exception
        }
      }

    }

    it should s"return good result in LLVM for file $filename" in {
      val llvmCode = llvmCompiler compile directory

      try {
        LlvmRunner.compile(llvmCode, directory)
        assert(LlvmRunner.run(directory) == fileWithResult.expectedResult)
      } catch {
        case exception: Exception => {
          println(llvmCode)
          throw exception
        }
      }


    }
  }
}
