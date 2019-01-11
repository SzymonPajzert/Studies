package compiler

import backend.{Directory, FileUtil, OutputDirectory}
import language.Type.{ArrayType, ClassType, IntType, PointerType}
import language.TypedLatte
import org.scalatest.FlatSpec
import parser.latte.LatteParser

class TypePhaseTest extends FlatSpec {
  behavior of "Type phase"

  private val typer =
    Compiler
      .debug("parser", LatteParser)
      .nextStage("parse_class", ParseClasses)
      .nextStage("typing", TypePhase)

  def lattestDir(filename: String): Directory = OutputDirectory.createTemporary.withSourceFile(
    s"$filename", FileUtil.readFile(FileUtil.testFile(s"lattests/$filename")))

  def testDir(filename: String): Directory = OutputDirectory.createTemporary.withSourceFile(
    s"$filename", FileUtil.readFile(FileUtil.testFile(s"latte/pos/$filename")))

  private val counterDirectory = OutputDirectory.createTemporary.withSourceFile(
    "counter.latte", FileUtil.readFile(FileUtil.testFile("latte/pos/class.latte")))


  def findGoodTypes(directory: Directory)(checkMain: TypedLatte.Func => Unit): Unit = {
    it should s"find good types in ${directory.sourceFile.getName} ${directory.directory}" in {
      val typedCode = typer compile directory match {
        case Right(x) => x
        case Left(x) => fail(s"Parser failed: $x")
      }

      val mainFunction = TypedLatte.findFunction(typedCode, "main") getOrElse fail("Could not find main")

      checkMain(mainFunction)
    }
  }

  def checkClasses(directory: Directory)(checkTypes: TypedLatte.CodeInformation => Unit): Unit = {
    it should s"parse classes for ${directory.sourceFile.getName} (${directory.directory})" in {
      val typedCode = typer compile directory match {
        case Right(x) => x
        case Left(x) => fail(s"Parser failed: $x")
      }

      checkTypes(typedCode._2)
    }
  }

  findGoodTypes(counterDirectory) { mainFunction =>
    val Some((_, cType)) = TypedLatte.findAssignment(mainFunction, "c0")
    val Some((_, xType)) = TypedLatte.findAssignment(mainFunction, "x0")
    assert(cType == PointerType(ClassType("Counter")))
    assert(xType == IntType)
  }

  findGoodTypes(testDir("type/functions.latte")) { mainFunction =>
    assert(TypedLatte.findAssignment(mainFunction, "x0").get._2 == IntType)
    assert(TypedLatte.findAssignment(mainFunction, "y0").get._2 == IntType)
    assert(TypedLatte.findAssignment(mainFunction, "z0").get._2 == IntType)
  }

  /* TODO
  findGoodTypes(testDir("type/arrays.latte")) { mainFunction =>
    assert(TypedLatte.findAssignment(mainFunction, "x0").get._2 == ArrayType(ArrayType(IntType)))
  }
  */

  checkClasses(testDir("struct/pair.latte")) { typeInformation =>
    assert(typeInformation.containedClasses.head == ClassType("Pair"))
  }

  checkClasses(lattestDir("extensions/objects2/shapes.lat")) { typeInformation =>
    val shapeMethods = typeInformation.method(ClassType("Shape")).elts
    val rectangleMethods = typeInformation.method(ClassType("Rectangle")).elts
    val circleMethods = typeInformation.method(ClassType("Circle")).elts
    val squareMethods = typeInformation.method(ClassType("Square")).elts

    assert(shapeMethods.length == 2)
    assert(rectangleMethods.length == 2)
    assert(circleMethods.length == 2)
    assert(squareMethods.length == 2)
  }
}
