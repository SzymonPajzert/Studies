package compiler

import backend.{Directory, FileMatchers, OutputDirectory}
import org.scalatest.{FlatSpec, Matchers}

class DebugCompilerTest extends FlatSpec with Matchers with FileMatchers {
  behavior of "Debug compiler"

  def integerCompiler: DebugCompiler[Int] = Compiler.debug("integer", (directory: Directory) => Right(5))

  def failingPhase: DebugCompiler[Int] = Compiler.debug("failing",
    (directory: Directory) => Left(List(ErrorString("failed to produce value"))))

  def toStringCompiler: Compiler[Int, String] = (value: Int) => Right(value.toString)

  it should "create intermediate files" in {
    val compiler = integerCompiler.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    outputDirectory.subfile("integer") should not be existingFile
    outputDirectory.subfile("string") should not be existingFile

    compiler compile outputDirectory

    outputDirectory.subfile("integer") should be an existingFile
    outputDirectory.subfile("string") should be an existingFile

    outputDirectory.subfile("integer") should be a nonemptyFile
    outputDirectory.subfile("string") should be a nonemptyFile
  }

  it should "stop creating files on error" in {
    val compiler = failingPhase.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    outputDirectory.subfile("failing") should not be existingFile
    outputDirectory.subfile("string") should not be existingFile

    compiler compile outputDirectory

    outputDirectory.subfile("failing") should not be existingFile
    outputDirectory.subfile("string") should not be existingFile
  }
  it should "generate error message on error" in {
    val compiler = failingPhase.nextStage("string", toStringCompiler)

    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source", "aaa")

    outputDirectory.subfile("failing.err") should not be existingFile

    compiler compile outputDirectory

    outputDirectory.subfile("failing.err") should be an existingFile
  }
}
