package compiler

import backend.{Directory, FileUtil, OutputDirectory}
import language.Type.{ArrayType, ClassType, IntType}
import language.TypedLatte
import org.scalatest.FlatSpec
import parser.latte.LatteParser

class TypePhaseTest extends FlatSpec {
  behavior of "Type phase"

  private val typer =
    Compiler
      .debug("parser", LatteParser)
      .nextStage("typing", TypePhase)

  def typeTestDir(filename: String): Directory = OutputDirectory.createTemporary.withSourceFile(
    s"$filename.latte", FileUtil.readFile(FileUtil.testFile(s"latte/pos/type/$filename.latte")))

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

  findGoodTypes(counterDirectory) { mainFunction =>
    val Some((_, cType)) = TypedLatte.findAssignment(mainFunction, "c")
    val Some((_, xType)) = TypedLatte.findAssignment(mainFunction, "x")
    assert(cType == ClassType("Counter"))
    // TODO assert(xType == IntType)
  }

  findGoodTypes(typeTestDir("functions")) { mainFunction =>
    assert(TypedLatte.findAssignment(mainFunction, "x0").get._2 == IntType)
    assert(TypedLatte.findAssignment(mainFunction, "y0").get._2 == IntType)
    assert(TypedLatte.findAssignment(mainFunction, "z0").get._2 == IntType)
  }

  findGoodTypes(typeTestDir("arrays")) { mainFunction =>
    assert(TypedLatte.findAssignment(mainFunction, "x0").get._2 == ArrayType(ArrayType(IntType)))
  }
}
