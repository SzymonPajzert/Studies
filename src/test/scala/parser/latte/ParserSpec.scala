// reviewed: 2018.12.29

package parser.latte

import integration.FileEnumerator
import integration.FileEnumerator.Test
import org.scalatest.{FlatSpec, Matchers}

class ParserSpec extends FlatSpec with Matchers {
  behavior of "Latte parser"

  /**
    * Check if given test contains file that should parse
    * @param requirements Test requirements
    */
  def parseFile(requirements: Test): Unit = {
    it should s"compile file: ${requirements.filename}" in {
      val parseResult = LatteParser.parse(requirements.fileContent)
      parseResult match {
        case Right(_) => Unit
        case Left(error) => fail(s"$error")
      }
    }
  }

  for (requirements <- FileEnumerator.getWithResult) {
    parseFile(requirements)
  }
}

