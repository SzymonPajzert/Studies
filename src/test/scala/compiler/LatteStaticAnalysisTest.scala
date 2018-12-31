package compiler

import language.Latte
import language.Type.IntType
import org.scalatest.{FlatSpec, Matchers}

class LatteStaticAnalysisTest extends FlatSpec with Matchers {
  behavior of "LatteStaticAnalysisTest"

  def makeMain(block: Latte.Block): Latte.Code = {
    Latte.Code(
      List(Latte.Func(Latte.FunctionSignature("main", IntType, List()), block)),
      null)
  }
}
