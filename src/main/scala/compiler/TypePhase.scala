package compiler

import compiler.TypePhase.TypeEnvironment
import language.Latte.TypeInformation
import language.Type._
import language.{Type, TypedLatte, UntypedLatte}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

/**
  * Performs following checks:
  *   - static binding of variables
  *   - renaming hiding variables
  *   - typing the expressions
  */
object TypePhase extends Compiler[UntypedLatte.Code, TypedLatte.Code] {
  case class VariableState(counter: Int, fromCurrentBlock: Boolean, typeId: Type)
  type VarBinds = Map[String, VariableState]
  type FunBinds = Map[String, Type]
  case class Binds(variables: VarBinds, functions: Map[String, Type])

  type S[A] = State[Binds, A]
  type TypeEnvironment[A] = EitherT[S, List[CompileException], A]

  implicit def viewableOnFirst[B, A <: B]
    (t: TypeEnvironment[(A, TypedLatte.ExpressionInformation)])
  : TypeEnvironment[(B, TypedLatte.ExpressionInformation)] = for {
    (value, information) <- t
  } yield (value, information)

  implicit def ok[A](value: A): TypeEnvironment[A] = EitherT[S, List[CompileException], A](state[Binds, List[CompileException] \/ A](\/-(value)))

  def createError[A](str: String): TypeEnvironment[A] = EitherT[S, List[CompileException], A](state(-\/(List(ErrorString(str)))))

  def mapM[A, B](list: List[A], f: A => TypeEnvironment[B]): TypeEnvironment[List[B]] = list match {
    case Nil => Nil
    case (hM :: tM) => for {
      h <- f(hM)
      t <- mapM(tM, f)
    } yield h :: t
  }

  def location(loc: UntypedLatte.LocationInf): TypeEnvironment[TypedLatte.LocationInf] = loc._1 match {
    case UntypedLatte.Variable(ident) => for {
      binds <- get[Binds] : TypeEnvironment[Binds]
      (identifier, typeId) <- (binds.variables get ident match {
        case Some(state) => (ident + state.counter.toString, state.typeId)
        case None => createError(s"Undefined variable $ident")
      }): TypeEnvironment[(String, Type)]
    } yield (TypedLatte.Variable(identifier), typeId)

    case UntypedLatte.ArrayAccess(arrayU, elementU) => for {
      array <- expression(arrayU)
      element <- expression(elementU)
    } yield (TypedLatte.ArrayAccess(array, element), VoidType)

    case UntypedLatte.FieldAccess(placeU, element) => for {
      place <- expression(placeU)
    } yield (TypedLatte.FieldAccess(place, element), VoidType)
  }

  implicit def stateToEither[A](value: State[Binds, A]): TypeEnvironment[A] = {
    EitherT[S, List[CompileException], A](value map (\/-(_)))
  }

  def lookupFunctionSignature(functionName: String): TypeEnvironment[Type] = for {
    signatures <- get[Binds] : TypeEnvironment[Binds]

    foundValue <- (signatures.functions get functionName match {
      case Some(value) => ok(value)
      case None => createError(s"Function: $functionName not defined. \nEnvironment: $signatures")
    }) : TypeEnvironment[Type]
  } yield foundValue

  def funLocation(fLoc: UntypedLatte.FunLocation): TypeEnvironment[TypedLatte.FunLocationInf] = fLoc match {
    case UntypedLatte.FunName(name: String) => for {
      functionType <- (name match {
        case "printInt" => FunctionType(VoidType, Seq(IntType))
        case "printString" => FunctionType (VoidType, Seq (StringType))
        case "int_add" => FunctionType (IntType, Seq (IntType, IntType))
        case "int_sub" => FunctionType (IntType, Seq (IntType, IntType))
        case "gen_gt" => FunctionType (BoolType, Seq (IntType, IntType))
        case "gen_lt" => FunctionType (BoolType, Seq (IntType, IntType))
        case userDefinedName => lookupFunctionSignature(userDefinedName)
      }) : TypeEnvironment[Type]
    } yield (TypedLatte.FunName(name), functionType)

    case UntypedLatte.VTableLookup(exprU, ident) => for {
      expr <- expression(exprU)
    } yield (TypedLatte.VTableLookup(expr, ident), VoidType)
  }

  def extractType(value: Any): Type = value match {
    case _ if value.isInstanceOf[Int] => Type.IntType
    case _ if value.isInstanceOf[Boolean] => Type.BoolType
    case _ if value.isInstanceOf[String] => Type.StringType
  }


  def returnType(functionName: String, t: Type): TypeEnvironment[Type] = t match {
    case FunctionType(rT, _) => rT
    case _ => createError(s"$functionName is not callable")
  }

  def expression(expr: UntypedLatte.ExpressionInf): TypeEnvironment[TypedLatte.ExpressionInf] = expr._1 match {
    case UntypedLatte.FunctionCall(locU , arguments) => for {
      loc <- funLocation(locU._1)
      args <- mapM(arguments.toList, expression)
      rt <- returnType(locU.toString, loc._2)

      // TODO check args match
    } yield (TypedLatte.FunctionCall(loc, args), rt)

    case UntypedLatte.ConstValue(value) => (TypedLatte.ConstValue(value), extractType(value))

    case UntypedLatte.ArrayCreation(elementType, size) => for {
      typedSize <- expression(size)
      result = (typedSize match {
        case (TypedLatte.ConstValue(v), Type.IntType) => {
          val arrayType = ArrayType(elementType, v.asInstanceOf[Int])
          (TypedLatte.ArrayCreation(elementType, typedSize), arrayType)
        }
      }) : TypedLatte.ExpressionInf
    } yield result

    case UntypedLatte.InstanceCreation(typeT) => (TypedLatte.InstanceCreation(typeT), typeT)
    case loc: UntypedLatte.Location => location((loc, Unit))
  }

  def markAsOld(binds: Binds): Binds = {
    binds.copy(variables = binds.variables.map {
      pair => (pair._1, pair._2.copy(fromCurrentBlock = false))
    })
  }

  def instruction(ins: UntypedLatte.Instruction): TypeEnvironment[TypedLatte.Instruction] = ins match {
    case UntypedLatte.Declaration(identifier, typeValue) => for {
      binds <- get[Binds]: TypeEnvironment[Binds]
      counter <- (binds.variables get identifier match {
        case Some(state) if state.fromCurrentBlock => createError(s"$identifier already defined")
        case Some(state) => for {
          _ <- modify[Binds]{binds => binds.copy(
            variables = binds.variables + (identifier -> VariableState(state.counter + 1, true, typeValue)))
          }
        } yield state.counter + 1
        case None => for {
          _ <- modify[Binds]{binds => binds.copy(
          variables = binds.variables + (identifier -> VariableState(0, true, typeValue)))}
        } yield 0
        }) : TypeEnvironment[Int]
    } yield TypedLatte.Declaration(identifier + counter.toString , typeValue)

    case UntypedLatte.Assignment(locU, exprU) => for {
      loc <- location(locU)
      expr <- expression(exprU)
    } yield TypedLatte.Assignment(loc, expr)

    case UntypedLatte.BlockInstruction(blockU) => (for {
      s <- get[Binds]: TypeEnvironment[Binds]
      blockE = mapM(blockU, instruction).run(markAsOld(s))._2
      block <- EitherT[S, List[CompileException], List[TypedLatte.Instruction]](state(blockE)): TypeEnvironment[List[TypedLatte.Instruction]]
    } yield TypedLatte.BlockInstruction(block)): TypeEnvironment[TypedLatte.Instruction]

    case UntypedLatte.DiscardValue(expr) => expression(expr) map TypedLatte.DiscardValue

    case UntypedLatte.Return(value) => value match {
      case None => TypedLatte.Return(None)
      case Some(v) => expression(v) map (vE => TypedLatte.Return(Some(vE)))
    }

    case UntypedLatte.IfThen(conditionU, thenInstU, elseUOpt) => for {
      condition <- expression(conditionU)
      thenInst <- instruction(thenInstU)
      elseOpt <- (elseUOpt match {
        case None => None
        case Some(els) => instruction(els) map (Some(_))
      }): TypeEnvironment[Option[TypedLatte.Instruction]]
    } yield TypedLatte.IfThen(condition, thenInst, elseOpt)

    case UntypedLatte.While(conditionU, instrU) => for {
      condition <- expression(conditionU)
      instr <- instruction(instrU)
    } yield TypedLatte.While(condition, instr)
  }

  def toplevelFunc: UntypedLatte.Func => TypeEnvironment[TypedLatte.Func] = {
    case UntypedLatte.Func(signature, codeU) => {
      val definitions: List[UntypedLatte.Instruction] =
        signature.arguments map { case (name, value) => UntypedLatte.Declaration(name, value): UntypedLatte.Instruction }

      for {
        _ <- mapM(definitions, instruction)
        code <- mapM(codeU, instruction)
      } yield {
        val sig = signature.asInstanceOf[TypedLatte.FunctionSignature]
        val args = sig.arguments map {case (name, t) => (name + "0", t)}
        TypedLatte.Func(sig.copy(arguments = args), code)
      }
    }
  }

  def member: UntypedLatte.ClassMember => TypeEnvironment[TypedLatte.ClassMember] = {
    case func : UntypedLatte.Func => for {
      f <- toplevelFunc(func)
    } yield f
    case UntypedLatte.Declaration(identifier, typeValue) =>
      TypedLatte.Declaration(identifier, typeValue)
  }

  def topDefinition: UntypedLatte.TopDefinition => TypeEnvironment[TypedLatte.TopDefinition] = {
    case func : UntypedLatte.Func => for {
      f <- toplevelFunc(func)
    } yield f
    case UntypedLatte.Class(name, base, insidesU) => for {
      insides <- mapM(insidesU, member)
    } yield TypedLatte.Class(name, base, insides)
  }

  def addTopDefinition: UntypedLatte.TopDefinition => TypeEnvironment[Unit] = {
    case UntypedLatte.Func(signature, _) =>
      val name = signature.identifier
      val typeId = FunctionType(signature.returnType, signature.arguments.map(_._2))
      for {
        binds <- get[Binds]: TypeEnvironment[Binds]
        _ <- (binds.functions get name match {
          case Some(_) => createError(s"$name already defined")
          case None => modify[Binds]{binds => binds.copy(functions = binds.functions + (name -> typeId))}
        }) : TypeEnvironment[Unit]
      } yield Unit
    case _ => ok(Unit)
  }

  def runWithSeparateStates(defs: List[UntypedLatte.TopDefinition],
                            state: Binds): TypeEnvironment[List[TypedLatte.TopDefinition]] = defs match {
    case Nil => Nil
    case (hM :: tM) => for {
      h <- topDefinition(hM)
      t <- runWithSeparateStates(tM, state)
    } yield h :: t
  }


  override def compile(code: UntypedLatte.Code): Either[List[CompileException], TypedLatte.Code] = {
    val typePhase: TypeEnvironment[TypedLatte.Code] = for {
      _ <- mapM(code._1.toList, addTopDefinition)
      s <- get[Binds]: TypeEnvironment[Binds]
      typedCode <- runWithSeparateStates(code._1.toList, s)
    } yield (typedCode, new TypeInformation)

    typePhase.run(Binds(Map(), Map()))._2.toEither
  }
}
