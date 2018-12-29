package integration

import backend.FileUtil
import compiler.{CompileException, ErrorString}

object FileEnumerator {
  def positiveTest(filename: String, expectedResult: List[String]): Test = {
    Test(filename, FileUtil.readFile(FileUtil.testFile(filename)), Right(expectedResult))
  }

  def negativeTest(filename: String, expectedError: List[CompileException]): Test = {
    Test(filename, FileUtil.readFile(FileUtil.testFile(filename)), Left(expectedError))
  }

  case class Test(filename: String,
                          fileContent: String,
                          expectedResult: Either[List[CompileException], List[String]])



  def getWithResult: List[Test] = List(
    positiveTest("latte/pos/assign_complex_expression.latte", List("4")),
    positiveTest("latte/pos/hiding.latte", List("1", "3")),
    positiveTest("latte/pos/loop.latte", List(
      "loop iteration", "3",
      "loop iteration", "2",
      "loop iteration", "1")),
    positiveTest("latte/pos/print.latte", List("Hello World")),
    negativeTest("latte/neg/undeclared.latte", List(ErrorString("Undefined variable z")))
  )
}
