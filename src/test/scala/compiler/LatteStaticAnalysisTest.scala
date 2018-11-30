package compiler

import language.Type.IntType
import org.scalatest.{FlatSpec, Matchers}
import parser.latte.Latte

class LatteStaticAnalysisTest extends FlatSpec with Matchers {
  behavior of "LatteStaticAnalysisTest"

  def makeMain(block: Latte.Block): Latte.Code = {
    List(
      Latte.Func(Latte.FunctionSignature("main", IntType, List()), block)
    )
  }
}
