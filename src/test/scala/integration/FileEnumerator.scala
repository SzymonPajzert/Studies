package integration

import java.io.File

import backend.FileUtil
import compiler.{CompileException, UndefinedVariable}
import org.scalatest.{Assertions, FlatSpec}

object Test extends Assertions {
  def checkListEq[A](result: List[A], expected: List[A]): Boolean = {
    assert(result.length == expected.length, s"wrong list length: $result")

    for (((resultElt, expectedElt), index) <- result.zip(expected).zipWithIndex) {
      if (resultElt != expectedElt) {
        fail(s"$resultElt != $expectedElt at line $index, $result")
      }
    }

    result == expected
  }

  def positive(filename: String, expectedResult: List[String]): Test = {
    val testFile = FileUtil.testFile(filename)
    Test(testFile, testFile.getName, FileUtil.readFile(testFile), {
      case Right(result) => checkListEq(result, expectedResult)
    }, true)
  }

  def anyNegative(filename: String, parseable: Boolean): Test = {
    val testFile = FileUtil.testFile(filename)
    Test(testFile, testFile.getName, FileUtil.readFile(testFile), {
      case Left(_) => Unit
    }, parseable)
  }

  def negative(filename: String, expectedError: CompileException => Boolean): Test = {
    val testFile = FileUtil.testFile(filename)
    Test(testFile, testFile.getName, FileUtil.readFile(testFile), {
      case Left(result) => assert(expectedError(result))
    }, true)
  }

  def parser(filename: String): Test = {
    val testFile = FileUtil.testFile(s"latte/pos/$filename")
    Test(testFile, testFile.getName, FileUtil.readFile(testFile), {case _ : Any => Unit}, true)
  }
}

case class Test(sourceFile: File,
                filename: String,
                fileContent: String,
                expectedResultPartial: PartialFunction[Either[CompileException, List[String]], Unit],
                parseable: Boolean) {
  import Assertions._

  def expectedResult: Either[CompileException, List[String]] => Unit =
    expectedResultPartial.orElse {
      case Right(x) => fail(s"Unexpected $x")
      case Left(x) => fail(s"Unexpected $x")
    }
}

object FileEnumerator {
  def testOnPath(path: String): Test = {
    import FileUtil._

    val expected = parseOut(readFile(testFile(s"$path.output")))
    Test.positive(s"$path.lat", expected)
  }

  def latteTestNegative(skip: Set[Int], nonparseable: Set[Int]): List[Test] = (1 to 27).toList filter (!skip.contains(_)) map { number =>
    val filename = f"lattests/bad/bad$number%03d"
    Test.anyNegative(s"$filename.lat", !(nonparseable contains number))
  }

  def latteTestPositive(skip: Set[Int]): List[Test] = (1 to 22).toList filter (!skip.contains(_)) map { number =>
    val filename = f"lattests/good/core$number%03d"
    testOnPath(filename)
  }

  def getWithResult: List[Test] =
    latteTestNegative(Set(14), Set(1, 2, 4, 5)) ++
    latteTestPositive(Set()) ++ List(
    Test.parser("array_function.latte"),
    Test.parser("class.latte"),
    Test.parser("foreach.latte"),
    Test.parser("inheritance.latte"),
    Test.parser("structures.latte"),
    Test.parser("virtual_methods.latte"),

    testOnPath("lattests/extensions/arrays1/array001"),
    testOnPath("lattests/extensions/arrays1/array002"),
    testOnPath("lattests/extensions/objects1/counter"),
    testOnPath("lattests/extensions/objects1/linked"),

    testOnPath("lattests/extensions/objects1/queue"),

    testOnPath("lattests/extensions/objects1/points"),
    testOnPath("lattests/extensions/objects2/baseAfterSubclass"),
    testOnPath("lattests/extensions/objects2/newMethod"),

    testOnPath("lattests/extensions/objects2/shapes"),

    testOnPath("lattests/extensions/struct/list"),
    Test.positive("lattests/extensions/struct/list_short0.lat", List()),
    Test.positive("lattests/extensions/struct/list_short1.lat", List("1", "2")),
    Test.positive("lattests/extensions/struct/list_short2.lat", List("1", "2", "3")),
    Test.positive("lattests/extensions/struct/list_short3.lat", List("1", "2", "3")),


      // TODO multidimenstional arrays
    Test.positive("latte/pos/changing_func_args.latte", List("2")),
    Test.positive("latte/pos/struct/recursive.latte", ((0 to 6) map (_.toString)).toList),
    Test.positive("latte/pos/struct/wrapper.latte", List("1")),
    Test.positive("latte/pos/struct/wrapper_methods.latte", List("1")),
    Test.positive("latte/pos/struct/wrapper_methods_this.latte", List("1")),
    Test.positive("latte/pos/struct/pair.latte", List("1")),
    Test.positive("latte/pos/struct/triple.latte", List("5")),
    Test.positive("latte/pos/struct/complex.latte", List("3")),
    Test.positive("latte/pos/type/functions.latte", List("3")),
    Test.positive("latte/pos/array_access.latte", List("1")),
    Test.positive("latte/pos/array_assign.latte", List()),
    Test.positive("latte/pos/array_loop.latte", List("1", "2", "3")),
    Test.positive("latte/pos/array_string.latte", List("aaaa")),
    Test.positive("latte/pos/assign_complex_expression.latte", List("4")),
    Test.positive("latte/pos/hiding.latte", List("1", "3")),
    Test.positive("latte/pos/loop.latte", List(
    "loop iteration", "3",
    "loop iteration", "2",
    "loop iteration", "1")),
    Test.positive("latte/pos/print.latte", List("Hello World")),
    Test.negative("latte/neg/undeclared.latte", { case UndefinedVariable(_, _) => true })
  )
}

class FileEnumeratorTest extends FlatSpec {
  behavior of "File enumerator"

  def assertSeqEq[A](result: Set[A], expected: Set[A]): Unit = {
    assert(result.diff(expected) == Set())
    val notSupportedFiles = expected.diff(result)
    assert(notSupportedFiles == Set(), notSupportedFiles.mkString("\n"))
  }

  /**
    *
    * @param file
    * @param f Filter on files (leaves)
    * @return
    */
  def recursiveListFiles(file: File, f: File => Boolean): Seq[File] = {
    if(file.isDirectory) {
      val directory = file
      directory.listFiles flatMap (file => recursiveListFiles(file, f))
    } else {
      Seq(file) filter f
    }
  }

  it should "enumerate all files" in {
    val enumeratedFiles = FileEnumerator.getWithResult.map(_.sourceFile.getCanonicalPath).toSet
    val allLatteFiles = recursiveListFiles(FileUtil.testFilesRoot, { file =>
      val s = file.getName
      s.endsWith(".latte") || s.endsWith(".lat")
    }).map(_.getCanonicalPath).toSet

    assertSeqEq(enumeratedFiles, allLatteFiles)
  }
}
