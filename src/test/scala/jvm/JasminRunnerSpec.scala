package jvm

import java.io._

import org.scalatest._

trait FileMatchers {

  import org.scalatest.matchers._

  class FileBePropertyMatcher extends BePropertyMatcher[java.io.File] {
    def apply(left: File): BePropertyMatchResult = {
      val fileNonempty = left.length() != 0
      BePropertyMatchResult(fileNonempty, "nonempty file")
    }
  }

  def nonemptyFile = new FileBePropertyMatcher
}

class FileMatchersTest extends FlatSpec with Matchers with FileMatchers {
  import java.io.BufferedWriter
  import java.io.FileWriter

  behavior of "FileMathchers"

  it should "detect empty files" in {
    val temporaryFile = File.createTempFile("source", "txt")

    val writer: BufferedWriter = new BufferedWriter(new FileWriter(temporaryFile))
    writer.write("hoho")
    writer.close()

    temporaryFile should be a nonemptyFile
  }
}

class JasminRunnerSpec extends FlatSpec with Matchers with FileMatchers {
  behavior of "JasminRunner"

  val validCode =
    """
      |.class  public Hello
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
      |  ldc "Hello"
      |  invokevirtual  java/io/PrintStream/println(Ljava/lang/String;)V
      |  return
      |.end method
    """.stripMargin

  it should "compileWithoutErrors" in {
    val mockFile = File.createTempFile("source", "j")

    val result = JasminRunner.compile(validCode, mockFile)

    mockFile should be a nonemptyFile
  }
}