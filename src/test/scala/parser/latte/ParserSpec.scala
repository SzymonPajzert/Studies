// reviewed: 2018.12.29

package parser.latte

import integration.{FileEnumerator, Test}
import org.scalatest.{FlatSpec, Matchers}

class ParserSpec extends FlatSpec with Matchers {
  behavior of "Latte parser"

  /**
    * Check if given test contains file that should parse
    * @param requirements Test requirements
    */
  def parseFile(requirements: Test): Unit = {
    it should s"parse file: ${requirements.sourceFile}" in {
      val parseResult = LatteParser.parse(requirements.fileContent)
      if (requirements.parseable) {
        parseResult match {
          case Right(_) => Unit
          case Left(error) => fail(s"$error")
        }
      } else {
        parseResult match {
          case Right(_) => fail(s"Should not be parsed")
          case Left(_) => Unit
        }
      }
    }
  }

  for (requirements <- FileEnumerator.getWithResult) {
    parseFile(requirements)
  }
}

