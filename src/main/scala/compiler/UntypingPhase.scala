// reviewed: 2018.12.28

package compiler

import language.{Latte, TypedLatte}

import scalaz.Scalaz._
import scalaz._

object UntypingPhase extends Compiler[TypedLatte.Code, Latte.Code] {
  type S[A] = State[Unit, A]
  type Compiler[E] = EitherT[S, List[CompileException], E]

  implicit def ok[A](value: A): Compiler[A] = EitherT[S, List[CompileException], A](state[Unit, List[CompileException] \/ A](\/-(value)))

  def mapM[A, B](list: List[A], f: A => Compiler[B]): Compiler[List[B]] =
    (list foldRight (ok(List()): Compiler[List[B]])) { (elt, acc) => for {
        newElt <- f(elt)
        okAcc <- acc
      } yield newElt :: okAcc
    }

  def compileExpr(expression: TypedLatte.ExpressionInf): Compiler[Latte.Expression] = {
    expression._1 match {
      case TypedLatte.Variable(identifier) => Latte.Variable(identifier)
      case TypedLatte.FunctionCall((TypedLatte.FunName(functionName), _), a) => for {
        arguments <- mapM(a.toList, compileExpr)
      } yield Latte.FunctionCall(Latte.FunName(functionName), arguments)
      case TypedLatte.ConstValue(v) => Latte.ConstValue(v)
      case TypedLatte.ArrayCreation(a, b) => compileExpr(b) map (Latte.ArrayCreation(a, _))
      case TypedLatte.ArrayAccess((TypedLatte.Variable(name), _), index) =>
        compileExpr(index) map (Latte.ArrayAccess(Latte.Variable(name), _))
    }
  }

  def instruction: TypedLatte.Instruction => Compiler[Latte.Instruction] = {
    case TypedLatte.Declaration(name, typeDecl) => Latte.Declaration(name, typeDecl)
    case TypedLatte.Assignment((TypedLatte.ArrayAccess((TypedLatte.Variable(name), _), indexE), _), expressionE) => for {
      index <- compileExpr(indexE)
      expression <- compileExpr(expressionE)
    } yield Latte.Assignment(Latte.ArrayAccess(Latte.Variable(name), index), expression)

    case TypedLatte.Assignment((TypedLatte.Variable(name), _), expressionE) => for {
      expression<- compileExpr(expressionE)
    } yield Latte.Assignment(name, expression): Latte.Instruction

    case TypedLatte.BlockInstruction(instructionsE) => mapM(instructionsE, instruction) map Latte.BlockInstruction

    case TypedLatte.DiscardValue(expression) => compileExpr(expression) map Latte.DiscardValue

    case TypedLatte.Return(value) => value match {
      case None => Latte.Return(None)
      case Some(v) => compileExpr(v) map (vE => Latte.Return(Some(vE)))
    }

    case TypedLatte.While(conditionE, instructionsE) => for {
      condition <- compileExpr(conditionE)
      instructions <- instruction(instructionsE)
    } yield Latte.While(condition, instructions)
  }

  def transformSignature(signature: TypedLatte.FunctionSignature): Latte.FunctionSignature =
    Latte.FunctionSignature(signature.identifier, signature.returnType, signature.arguments)

  def compileFunc(func: TypedLatte.TopDefinition): Compiler[Latte.Func] = {
    func match {
      case TypedLatte.Func(signature, code) => for {
        codeParsed <- mapM(code, instruction)
      } yield Latte.Func(transformSignature(signature), codeParsed)
    }
  }

  override def compile(code: TypedLatte.Code): Either[List[CompileException], Latte.Code] = {
    val untyping = for {
      latteCode <- mapM(code._1.toList, compileFunc)
    } yield Latte.Code(latteCode)

    untyping.run(Unit)._2.toEither
  }
}
