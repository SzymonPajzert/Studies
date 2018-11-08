package parser.instant

import backend.FileUtil
import org.scalatest.{FlatSpec, Matchers}

object FileEnumerator {
  def getPos: List[String] = getWithResult map (_.filename)

  case class FileWithRequirements(filename: String, expectedResult: List[Int], stackDepth: Int)

  def testExample(i: Int): Option[FileWithRequirements] = {
    val filename = f"examples/ex$i%03d"

    val inputFile = s"$filename.ins"
    val outputFile = s"$filename.output"

    def parseFile(str: String): List[Int] = {
      FileUtil.readTestFile(str).split('\n').map(java.lang.Integer.valueOf(_).intValue).toList
    }

    if (FileUtil.existsTestFile(inputFile) && FileUtil.existsTestFile(outputFile)) {
      Some(FileWithRequirements(inputFile, parseFile(outputFile), -1))
    } else None
  }

  def getWithResult: List[FileWithRequirements] = ((1 to 10) flatMap testExample).toList ::: List(
    FileWithRequirements("pos/big_addition.inst", List(55), 11),
    FileWithRequirements("pos/same_variable.inst", List(1, 2, 4, 3), 3),
    FileWithRequirements("pos/simple.inst", List(2), 3),
    FileWithRequirements("pos/test01.ins", List(42), -1),
    FileWithRequirements("pos/test02.ins", List(42), -1),
    FileWithRequirements("pos/test03.ins", List(42), -1),
    FileWithRequirements("pos/test04.ins", List(42), -1),
    FileWithRequirements("pos/test05.ins", List(1), -1),
    FileWithRequirements("pos/test06.ins", List(2, 2), -1),
    FileWithRequirements("pos/two_commands.inst", List(3), 3),
    FileWithRequirements("pos/two_variables.inst", List(6), 3),
  )
}

class ParserSpec extends FlatSpec with Matchers {
  behavior of "Parser"

  def nInstructions(parseResult: Parser.ParseResult, requestedSize: Int): Unit = {
    parseResult match {
      case Right(error) => fail(s"Should not be right: $error")
      case Left(leftValue) if leftValue.lengthCompare(requestedSize) == 0 => Unit
      case Left(leftValue) => fail(s"Unrecognized value $leftValue")
    }
  }


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
