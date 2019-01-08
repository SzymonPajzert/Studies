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

  def testOnPath(path: String): Test = {
    import FileUtil._

    val expected = parseOut(readFile(testFile(s"$path.output")))
    positiveTest(s"$path.lat",expected)
  }

  def latteTestPositive(skip: Set[Int]): List[Test] = (1 to 22).toList filter (!skip.contains(_)) map { number =>
    val filename = f"lattests/good/core$number%03d"
    testOnPath(filename)
  }

  def getWithResult: List[Test] =
    latteTestPositive(Set(
      18,
      19,
      12) // TODO string class
    ) ++ List(
    parserTest("array_function.latte"),
    parserTest("class.latte"),
    parserTest("foreach.latte"),
    parserTest("inheritance.latte"),
    parserTest("structures.latte"),
    parserTest("virtual_methods.latte"),

    testOnPath("lattests/extensions/arrays1/array001"),
    testOnPath("lattests/extensions/arrays1/array002"),
    testOnPath("lattests/extensions/objects1/counter"),
    testOnPath("lattests/extensions/objects1/linked"),
    testOnPath("lattests/extensions/objects1/points"),
    // TODO testOnPath("lattests/extensions/objects1/queue"),

    // TODO inheritance in type system testOnPath("lattests/extensions/objects2/shapes"),
    testOnPath("lattests/extensions/struct/list"),


      // TODO multidimenstional arrays
    positiveTest("latte/pos/changing_func_args.latte", List("2")),
    // TODO positiveTest("latte/pos/struct/recursive.latte", ((1 to 8) map (_.toString)).toList),
    positiveTest("latte/pos/struct/wrapper.latte", List("1")),
    positiveTest("latte/pos/struct/pair.latte", List("1")),
    positiveTest("latte/pos/struct/triple.latte", List("5")),
    positiveTest("latte/pos/struct/complex.latte", List("3")),
    positiveTest("latte/pos/type/functions.latte", List("3")),
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
