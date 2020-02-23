package compiler

import language.Type.{FunctionType, IntType, VoidType}
import language.TypedLatte

object CheckMain extends Compiler[TypedLatte.Code, TypedLatte.Code] {
  def findMain(code: TypedLatte.Code): Either[CompileException, TypedLatte.Func] =
    code._1 find { case f: TypedLatte.Func => f.signature.identifier == "main" } match {
      case None => Left(NoMainFunction)
      case Some(main: TypedLatte.Func) => {
        val signature = FunctionType(main.signature.returnType, main.signature.arguments map (_._2))
        signature match {
          case FunctionType(IntType, Seq()) => Right(main)
          case _ => Left(WrongMainSignature(signature))
        }
      }
    }

  override def compile(code: TypedLatte.Code): Either[CompileException, TypedLatte.Code] = {
    for {
      _ <- findMain(code)
    } yield code
  }
}
