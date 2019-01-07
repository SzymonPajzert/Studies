package compiler

import language.Latte
import language.Latte.TypeInformation
import language.Type.IntType
import org.scalatest.{FlatSpec, Matchers}

class UntypingPhaseTest extends FlatSpec with Matchers {
  behavior of "LatteStaticAnalysisTest"

  def makeMain(block: Latte.Block): Latte.Code = {
    Latte.Code(
      List(Latte.Func(Latte.FunctionSignature("main", IntType, List()), block)),
      "",
      TypeInformation.empty)
  }
}
