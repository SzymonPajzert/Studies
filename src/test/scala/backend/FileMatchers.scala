package backend

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

trait FileMatchers {
  import org.scalatest.matchers._

  def nonemptyFile: BePropertyMatcher[File] = (left: File) => {
    val fileNonempty = left.length() != 0
    BePropertyMatchResult(fileNonempty, "nonempty file")
  }

  def existingFile: BePropertyMatcher[File] = (left: File) => {
    BePropertyMatchResult(left.exists(), "existing file")
  }

  def existingDirectory: BePropertyMatcher[File] = (left: File) => {
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