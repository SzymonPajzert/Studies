package backend.jvm

import backend.{FileMatchers, OutputDirectory}
import org.scalatest._

object Definitions {
  def validCode: String =
    """
      |.class  public Main
      |.super  java/lang/Object
      |
      |; standard initializer
      |.method public <init>()V
      |  aload_0
      |  invokespecial java/lang/Object/<init>()V
      |  return
      |.end method
      |
      |.method public static main([Ljava/lang/String;)V
      |.limit stack 2
      |.limit locals 3
      |  getstatic  java/lang/System/out Ljava/io/PrintStream;
      |  ldc "Hello world"
      |  invokevirtual  java/io/PrintStream/println(Ljava/lang/String;)V
      |  return
      |.end method
      |""".stripMargin
}

/*
TODO

class JasminRunnerSpec extends FlatSpec with Matchers with FileMatchers {
  import Definitions.validCode

  behavior of "JasminRunner"

  it should "compileWithoutErrors" in {
    val outputDirectory = OutputDirectory.createTemporary

    val result = JasminRunner.compile(validCode, outputDirectory)

    outputDirectory.jasminFile should be a nonemptyFile
    assert(outputDirectory.directory.listFiles().length == 2)

    val output = JasminRunner.run(outputDirectory)
    assert(output == "Hello world")
  }

  it should "be able to just run" in {
    assert(JasminRunner.runCode(validCode) == "Hello world")
  }
}
*/

class OutputDirectorySpec extends FlatSpec with Matchers with FileMatchers {
  behavior of "Output Directory"

  it should "create temporary directory" in {
    val outputDirectory = OutputDirectory.createTemporary

    outputDirectory.path should be an existingDirectory
  }

  it should "create source file if asked" in {
    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("source.inst", "this is test")

    outputDirectory.path should be an existingDirectory

    outputDirectory.sourceFile should be a nonemptyFile
  }

  it should "create source file with separator if asked" in {
    val outputDirectory = OutputDirectory.createTemporary.withSourceFile("path/source.inst", "this is test")

    outputDirectory.path should be an existingDirectory

    outputDirectory.sourceFile should be a nonemptyFile
  }
}