package compiler

import language.Type._
import language.{LatteCompiler, TypeInformation, TypedLatte}

import scalaz._

object CheckReturns extends Compiler[TypedLatte.Code, TypedLatte.Code] {
  def is(condition: TypedLatte.ExpressionInf, boolean: Boolean): Boolean = condition match {
    case (TypedLatte.ConstValue(b), _) if b == boolean => true
    case _ => false
  }

  class ReturnPath(val typeInformation: TypeInformation, val returnType: Type) {
    // Exists some path that achieves return
    def returnAchievable: TypedLatte.Block => Boolean = {
      case Nil => false
      case TypedLatte.BlockInstruction(subblock) :: rest => returnAchievable(subblock) || returnAchievable(rest)
      case TypedLatte.Return(_) :: _ => true
      case (ifThen : TypedLatte.IfThen) :: rest =>
        val thenIsCovered = returnAchievable(List(ifThen.thenInst))
        val elseIsCovered = ifThen.elseOpt match {
          case None => false
          case Some(els) => returnAchievable(List(els))
        }

        Unit match {
          case _ if is(ifThen.condition, true) => thenIsCovered || returnAchievable(rest)
          case _ if is(ifThen.condition, false) => elseIsCovered || returnAchievable(rest)
          case _ => (thenIsCovered ||elseIsCovered) || returnAchievable(rest)
        }
      case TypedLatte.While(condition, _) :: rest if is(condition, false) => returnAchievable(rest)
      case TypedLatte.While(condition, instr) :: rest if is(condition, true) =>
        returnAchievable(List(instr)) || returnAchievable(rest)
      case TypedLatte.While(_, instr) :: rest => returnAchievable(List(instr)) || returnAchievable(rest)
      case _ :: rest => returnAchievable(rest)
    }

    // Every execution path achieves return
    def returnCovers: TypedLatte.Block => Boolean = {
      case Nil => false
      case TypedLatte.BlockInstruction(subblock) :: rest => returnAchievable(subblock) || returnAchievable(rest)
      case TypedLatte.Return(_) :: _ => true
      case (ifThen : TypedLatte.IfThen) :: rest =>
        val thenIsCovered = returnAchievable(List(ifThen.thenInst))
        val elseIsCovered = ifThen.elseOpt match {
          case None => false
          case Some(els) => returnAchievable(List(els))
        }

        Unit match {
          case _ if is(ifThen.condition, true) => thenIsCovered || returnAchievable(rest)
          case _ if is(ifThen.condition, false) => elseIsCovered || returnAchievable(rest)
          case _ => (thenIsCovered && elseIsCovered) || returnAchievable(rest)
        }
      case TypedLatte.While(condition, _) :: rest if is(condition, false) => returnAchievable(rest)
      case TypedLatte.While(condition, instr) :: rest if is(condition, true) =>
        returnAchievable(List(instr)) || returnAchievable(rest)
      case TypedLatte.While(_, instr) :: rest => returnAchievable(List(instr)) || returnAchievable(rest)
      case _ :: rest => returnAchievable(rest)
    }
  }





  def checkReturnInFunction(typeInformation: TypeInformation): TypedLatte.TopDefinition => Either[CompileException, TypedLatte.TopDefinition] = {
    case func: TypedLatte.Func if func.signature.returnType == VoidType => {
      Right(addReturn(func))
    }

    case func: TypedLatte.Func => new ReturnPath(typeInformation, func.signature.returnType).returnCovers(func.code) match {
      case bool => if(bool) Right(addReturn(func)) else Left(MissingReturn(func.signature.identifier))
    }

  }

  def addReturn: TypedLatte.TopDefinition => TypedLatte.TopDefinition = {
    case f @ TypedLatte.Func(signature, instructions) => {
      val returnType = signature.returnType

      if (instructions.isEmpty || !instructions.last.isInstanceOf[TypedLatte.Return]) {
        val returnInst = returnType match {
          case VoidType => TypedLatte.Return(None)
          case IntType => TypedLatte.Return(Some((TypedLatte.ConstValue(0), returnType)))
          case ptrType : PointerType => TypedLatte.Return(Some((TypedLatte.Null(ptrType), returnType)))
        }

        f.copy(code = instructions ::: List(returnInst))
      } else f
    }
  }

  override def compile(code: TypedLatte.Code): Either[CompileException, TypedLatte.Code] = {
    val mapped = code._1.toList map checkReturnInFunction(code._2)
    val errors = mapped collect { case Left(error) => error }

    errors match {
      case List() => Right((mapped collect { case Right(value) => value }, code._2))
      case h :: _ => Left(h)
    }
  }
}
