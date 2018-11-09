package parser.latte

import backend.FileUtil
import org.scalatest.{FlatSpec, Matchers}

object FileEnumerator {
  def getPos: List[String] = getWithResult map (_.filename)

  case class FileWithRequirements(filename: String, expectedResult: List[String])

  def getWithResult: List[FileWithRequirements] = List(
    FileWithRequirements("latte/pos/print.latte", List("Hello World")),
  )
}

class ParserSpec extends FlatSpec with Matchers {
  behavior of "Latte parser"

  def compileFile(filename: String): Unit = {
    it should s"compile file: $filename" in {
      val fileContent = FileUtil.readTestFile(filename)

      val parseResult = Parser.parse(fileContent)
      parseResult match {
        case Left(_) => Unit
        case Right(error) => fail(s"Right: $error")
      }
    }
  }

  for (filename <- FileEnumerator.getPos) {
    compileFile(filename)
  }
}

