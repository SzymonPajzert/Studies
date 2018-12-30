package integration

import backend.FileUtil
import compiler.{CompileException, ErrorString}

object FileEnumerator {
  def positiveTest(filename: String, expectedResult: List[String]): Test = {
    val testFile = FileUtil.testFile(filename)
    Test(testFile.getName, FileUtil.readFile(testFile), Some(Right(expectedResult)))
  }

  def negativeTest(filename: String, expectedError: List[CompileException]): Test = {
    val testFile = FileUtil.testFile(filename)
    Test(testFile.getName, FileUtil.readFile(testFile), Some(Left(expectedError)))
  }

  def parserTest(filename: String): Test = {
    val testFile = FileUtil.testFile(s"latte/pos/$filename")
    Test(testFile.getName, FileUtil.readFile(testFile), None)
  }

  case class Test(filename: String,
                  fileContent: String,
                  expectedResult: Option[Either[List[CompileException], List[String]]])



  def getWithResult: List[Test] = List(
    parserTest("array_function.latte"),
    parserTest("class.latte"),
    parserTest("foreach.latte"),
    parserTest("inheritance.latte"),
    parserTest("structures.latte"),
    parserTest("virtual_methods.latte"),

    // TODO multidimenstional arrays
    positiveTest("latte/pos/array_access.latte", List("1")),
    positiveTest("latte/pos/array_assign.latte", List()),
    positiveTest("latte/pos/array_loop.latte", List("1", "2", "3")),
    positiveTest("latte/pos/array_string.latte", List("aaaa")),
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
