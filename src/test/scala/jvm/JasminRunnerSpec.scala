package jvm

import java.io._

import org.scalatest._

import scala.util.Random

trait FileMatchers {
  import org.scalatest.matchers._

  def nonemptyFile: BePropertyMatcher[File] = (left: File) => {
    val fileNonempty = left.length() != 0
    BePropertyMatchResult(fileNonempty, "nonempty file")
  }

  def existingFile: BePropertyMatcher[File] = (left: File) => {
    BePropertyMatchResult(left.exists(), "existing file")
  }
}

class FileMatchersTest extends FlatSpec with Matchers with FileMatchers {
  import java.io.{BufferedWriter, FileWriter}

  behavior of "FileMathchers"

  it should "detect empty files" in {
    val temporaryFile = File.createTempFile("source", "txt")

    val writer: BufferedWriter = new BufferedWriter(new FileWriter(temporaryFile))
    writer.write("hoho")
    writer.close()

    temporaryFile should be a nonemptyFile
  }
}

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
      |  getstatic  java/lang/System/out Ljava/io/PrintStream;
      |  ldc "Hello world"
      |  invokevirtual  java/io/PrintStream/println(Ljava/lang/String;)V
      |  return
      |.end method
      |""".stripMargin
}

class JasminRunnerSpec extends FlatSpec with Matchers with FileMatchers {
  import Definitions.validCode

  behavior of "JasminRunner"

  it should "compileWithoutErrors" in {
    val rand = new Random
    val directoryCounter = rand.nextInt()

    val outputDirectory = OutputDirectory.create(new File(s"/tmp/mrjp$directoryCounter"))

    val result = JasminRunner.compile(validCode, outputDirectory)

    outputDirectory.sourceFile should be a nonemptyFile
    assert(outputDirectory.directory.listFiles().length == 2)

    val output = JasminRunner.runJava(outputDirectory)
    assert(output == "Hello world")
  }

  it should "be able to just run" in {
    assert(JasminRunner.run(validCode) == "Hello world")
  }
}