package parser.latte

import backend.FileUtil
import org.scalatest.{FlatSpec, Matchers}
import parser.instant.{InstantParser, FileEnumerator => FE}
import parser.latte.FileEnumerator.FileWithRequirements


object FileEnumerator {
  object FileWithRequirements {
    def fromFile(filename: String, expectedResult: List[String]): FileWithRequirements = {
      FileWithRequirements(filename, FileUtil.readTestFile(filename), expectedResult)
    }
  }

  case class FileWithRequirements(filename: String,
                                  fileContent: String,
                                  expectedResult: List[String])

  def convert(oldRequirements: FE.FileWithRequirements): FileWithRequirements = {
    val Right(parsed) = InstantParser.parse(FileUtil.readTestFile(oldRequirements.filename))

    val lines = (parsed map (instant => instant.toSource)).mkString("\n  ")

    val newContent =
      s"""
         |int main() {
         |  $lines;
         |}
       """.stripMargin

    FileWithRequirements(
      oldRequirements.filename,
      newContent,
      oldRequirements.expectedResult map Integer.toString)
  }

  def getWithResult: List[FileWithRequirements] = List(
    FileWithRequirements.fromFile("latte/pos/print.latte", List("Hello World")),
    FileWithRequirements.fromFile("latte/pos/loop.latte", List(
      "loop iteration", "3",
      "loop iteration", "2",
      "loop iteration", "1"))
  )
}

class ParserSpec extends FlatSpec with Matchers {
  behavior of "Latte parser"

  def compileFile(requirements: FileWithRequirements): Unit = {
    it should s"compile file: ${requirements.filename}" in {
      val parseResult = LatteParser.parse(requirements.fileContent)
      parseResult match {
        case Right(topDefinitions) => {
          assert(topDefinitions.size === 1)
          topDefinitions.head match {
            case Latte.Func(_, _) => Unit
            case otherCase => fail(s"Unexpected head: $otherCase")
          }
        }
        case Left(error) => fail(s"Right: $error")
      }
    }
  }

  for (requirements <- FileEnumerator.getWithResult) {
    compileFile(requirements)
  }
}

