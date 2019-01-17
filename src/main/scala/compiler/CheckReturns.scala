package compiler

import language.Type._
import language.{LatteCompiler, TypedLatte}

import scalaz._

object CheckReturns extends Compiler[TypedLatte.Code, TypedLatte.Code] {
  def is(condition: TypedLatte.ExpressionInf, boolean: Boolean): Boolean = condition match {
    case (TypedLatte.ConstValue(b), _) if b == boolean => true
    case _ => false
  }

  def returnCovers(block: TypedLatte.Block, returnType: Type): WrongReturnType \/ Boolean = {
    def recursive(subblock: TypedLatte.Block) = returnCovers(subblock, returnType)

    type PackBool = WrongReturnType \/ Boolean
    def or(left: PackBool, right: PackBool): PackBool = for { l <- left; r <- right } yield l || r
    def and(left: PackBool, right: PackBool): PackBool = for { l <- left; r <- right } yield l && r


    block match {
      case Nil => \/-(false)
      case TypedLatte.BlockInstruction(subblock) :: rest => or(recursive(subblock), recursive(rest))
      case TypedLatte.Return(Some((_, t))) :: _   => if(returnType == t) \/-(true) else -\/(WrongReturnType(returnType, t))
      case TypedLatte.Return(None) :: _           => if(returnType == VoidType) \/-(true) else -\/(WrongReturnType(returnType, VoidType))
      case (ifThen : TypedLatte.IfThen) :: rest =>
        val thenIsCovered = recursive(List(ifThen.thenInst))
        val elseIsCovered = ifThen.elseOpt match {
          case None => \/-(false)
          case Some(els) => recursive(List(els))
        }

        Unit match {
          case _ if is(ifThen.condition, true) => or(thenIsCovered, recursive(rest))
          case _ if is(ifThen.condition, false) => or(elseIsCovered, recursive(rest))
          case _ => or(and(thenIsCovered, elseIsCovered), recursive(rest))
        }
      case TypedLatte.While(condition, _) :: rest if is(condition, false) => recursive(rest)
      case TypedLatte.While(_, instr) :: rest => or(recursive(List(instr)), recursive(rest))
      case _ :: rest => recursive(rest)
    }
  }

  def checkReturnInFunction: TypedLatte.TopDefinition => Either[CompileException, TypedLatte.TopDefinition] = {
    case func: TypedLatte.Func if func.signature.returnType == VoidType =>
      Right(addReturn(func))

    case func: TypedLatte.Func => returnCovers(func.code, func.signature.returnType) match {
      case -\/(error) => Left(error)
      case \/-(bool) => if(bool) Right(addReturn(func)) else Left(MissingReturn(func.signature.identifier))
    }
  }

  def addReturn: TypedLatte.TopDefinition => TypedLatte.TopDefinition = {
    case f @ TypedLatte.Func(signature, instructions) => {
      val returnType = signature.returnType

      if (instructions.isEmpty || !instructions.last.isInstanceOf[TypedLatte.Return]) {
        val returnInst = returnType match {
          case VoidType => TypedLatte.Return(None)
          case IntType => TypedLatte.Return(Some((TypedLatte.ConstValue(0), returnType)))
          case PointerType(_) => TypedLatte.Return(Some((TypedLatte.Null(returnType.deref.asInstanceOf[ClassType]), returnType)))
        }

        f.copy(code = instructions ::: List(returnInst))
      } else f
    }
  }

  override def compile(code: TypedLatte.Code): Either[CompileException, TypedLatte.Code] = {
    val mapped = code._1.toList map checkReturnInFunction
    val errors = mapped collect { case Left(error) => error }

    errors match {
      case List() => Right((mapped collect { case Right(value) => value }, code._2))
      case h :: _ => Left(h)
    }
  }
}
