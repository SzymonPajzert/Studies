package compiler

import backend.{Directory, FileMatchers, OutputDirectory}
import org.scalatest.{FlatSpec, Matchers}

class DebugCompilerTest extends FlatSpec with Matchers with FileMatchers {
  behavior of "Debug compiler"

  def integerCompiler: DebugCompiler[Int] = Compiler.debug("integer", (directory: Directory) => Right(5))

  def failingPhase: DebugCompiler[Int] = Compiler.debug("failing",
    (directory: Directory) => Left(UndefinedFunction("undefined_function", null)))

  def toStringCompiler: Compiler[Int, String] = (value: Int) => Right(value.toString)

  it should "create intermediate files" in {
    val compiler = integerCompiler.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    val integerFile = outputDirectory.subfile("0_integer")
    val stringFile = outputDirectory.subfile("1_string")

    integerFile should not be existingFile
    stringFile should not be existingFile

    compiler compile outputDirectory

    integerFile should be an existingFile
    stringFile should be an existingFile

    integerFile should be a nonemptyFile
    stringFile should be a nonemptyFile
  }

  it should "stop creating files on error" in {
    val compiler = failingPhase.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    val failingFile = outputDirectory.subfile("0_failing")
    val stringFile = outputDirectory.subfile("1_string")

    failingFile should not be existingFile
    stringFile should not be existingFile

    compiler compile outputDirectory

    failingFile should not be existingFile
    stringFile should not be existingFile
  }
  it should "generate error message on error" in {
    val compiler = failingPhase.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    outputDirectory.subfile("0_failing.err") should not be existingFile

    compiler compile outputDirectory

    outputDirectory.subfile("0_failing.err") should be an existingFile
  }
}
