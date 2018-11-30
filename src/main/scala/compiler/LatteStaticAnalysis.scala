package compiler

import parser.latte.Latte
import parser.latte.Latte.Code

object LatteStaticAnalysis extends Compiler[Latte.Code, Latte.Code] {
  override def compile(code: Code): Either[List[CompileException], Code] = Right(code)
}
