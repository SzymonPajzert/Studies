package compiler

import compiler.TypePhase.TypeEnvironment
import language.Latte.{FieldOffset, TypeInformation}
import language.Type._
import language.{Latte, Type, TypedLatte, UntypedLatte}

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

  type KS = (Binds, TypeInformation)

  type S[A] = State[KS, A]
  type TypeEnvironment[A] = EitherT[S, List[CompileException], A]

  implicit def viewableOnFirst[B, A <: B]
    (t: TypeEnvironment[(A, TypedLatte.ExpressionInformation)])
  : TypeEnvironment[(B, TypedLatte.ExpressionInformation)] = for {
    (value, information) <- t
  } yield (value, information)

  implicit def ok[A](value: A): TypeEnvironment[A] =
    EitherT[S, List[CompileException], A](state[KS, List[CompileException] \/ A](\/-(value)))

  implicit def stateToEither[A](value: State[KS, A]): TypeEnvironment[A] = {
    EitherT[S, List[CompileException], A](value map (\/-(_)))
  }

  def createError[A](str: String): TypeEnvironment[A] = EitherT[S, List[CompileException], A](state(-\/(List(ErrorString(str)))))

  def mapM[A, B](list: List[A], f: A => TypeEnvironment[B]): TypeEnvironment[List[B]] = list match {
    case Nil => Nil
    case (hM :: tM) => for {
      h <- f(hM)
      t <- mapM(tM, f)
    } yield h :: t
  }

  def getBinds: TypeEnvironment[Binds] = stateToEither(get[KS] map (_._1))

  def modifyBinds(f: Binds => Binds): TypeEnvironment[Unit] =
    modify[KS](ks => ks.copy(_1 =  f(ks._1)))

  def modifyTypeInformation(f: TypeInformation => TypeInformation): TypeEnvironment[Unit] =
    modify[KS](ks => ks.copy(_2 =  f(ks._2)))

  def location(loc: UntypedLatte.LocationInf): TypeEnvironment[TypedLatte.LocationInf] = loc._1 match {
    case UntypedLatte.Variable(ident) => for {
      binds <- getBinds
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
      typeInformation <- getTypeInformation
      elementType <- (place._2 match {
        case c @ ClassType(_) => typeInformation.fieldType(c, element) match {
          case Some(t) => t
          case None => createError(s"No field $element in $c")
        }
        case PointerType(c @ ClassType(_)) => typeInformation.fieldType(c, element) match {
          case Some(t) => t
          case None => createError(s"No field $element in $c")
        }
        case t => createError(s"Expected class, instead: $t")
      }) : TypeEnvironment[Type]
    } yield (TypedLatte.FieldAccess(place, element), elementType)
  }



  def lookupFunctionSignature(functionName: String): TypeEnvironment[Type] = for {
    signatures <- getBinds

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

  def assertType(inf: TypedLatte.ExpressionInf, expectedType: Type): TypeEnvironment[Unit] =
    if (inf._2 == expectedType) ok(Unit) else createError("Wrong type")

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
      _ <- assertType(typedSize, IntType)
      arrayType = ArrayType(elementType)
    } yield (TypedLatte.ArrayCreation(elementType, typedSize), arrayType)

    case UntypedLatte.InstanceCreation(typeT) => (TypedLatte.InstanceCreation(typeT), typeT)

    case loc: UntypedLatte.Location => location((loc, Unit))
  }

  def markBindsAsOld(state: KS): KS = {
    val binds = state._1

    val newBinds = binds.copy(variables = binds.variables.map {
      pair => (pair._1, pair._2.copy(fromCurrentBlock = false))
    })

    (newBinds, state._2)
  }

  def setCounter(identifier: String, counter: Int, typeValue: Type): TypeEnvironment[Int] = {
    (for {
      _ <- modifyBinds { binds => binds.copy(
        variables = binds.variables + (identifier -> VariableState(counter, true, typeValue)))
      }
    } yield counter) : TypeEnvironment[Int]
  }

  def instruction(ins: UntypedLatte.Instruction): TypeEnvironment[TypedLatte.Instruction] = ins match {
    case UntypedLatte.Declaration(identifier, typeValue) => for {
      binds <- getBinds
      counter <- (binds.variables get identifier match {
        case Some(state) if state.fromCurrentBlock => createError(s"$identifier already defined")
        case Some(state) => setCounter(identifier, state.counter + 1, typeValue)
        case None => setCounter(identifier, 0, typeValue)
        }) : TypeEnvironment[Int]
    } yield TypedLatte.Declaration(identifier + counter.toString , typeValue)

    case UntypedLatte.Assignment(locU, exprU) => for {
      loc <- location(locU)
      expr <- expression(exprU)
    } yield TypedLatte.Assignment(loc, expr)

    case UntypedLatte.BlockInstruction(blockU) => (for {
      s <- get[KS]: TypeEnvironment[KS]
      blockE = mapM(blockU, instruction).run(markBindsAsOld(s))._2
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

  def subclass(name: String, base: String): TypeEnvironment[Unit] = ok(Unit)

  def parseClassStructure(className: String, members: List[UntypedLatte.ClassMember]): TypeEnvironment[Unit] = {
    val identifierTypePairs = members map {
      case UntypedLatte.Declaration(value, c : ClassType) => (value, PointerType(c))
      case UntypedLatte.Declaration(value, t) => (value, t)
    }

    modifyTypeInformation { typeInformation =>
      new TypeInformation(
        typeInformation.defined + (ClassType(className) -> FieldOffset(identifierTypePairs))
      )
    }
  }

  def topDefinition: UntypedLatte.TopDefinition => TypeEnvironment[Option[TypedLatte.TopDefinition]] = {
    case func : UntypedLatte.Func => for {
      f <- toplevelFunc(func)
    } yield Some(f)
    case _ => ok(None)
  }

  def addTopDefinition: UntypedLatte.TopDefinition => TypeEnvironment[Unit] = {
    case UntypedLatte.Func(signature, _) =>
      val name = signature.identifier
      val typeId = FunctionType(signature.returnType, signature.arguments.map(_._2))
      for {
        binds <- getBinds: TypeEnvironment[Binds]
        _ <- (binds.functions get name match {
          case Some(_) => createError(s"$name already defined")
          case None => modifyBinds { binds => binds.copy(
            functions = binds.functions + (name -> typeId))}
        }) : TypeEnvironment[Unit]
      } yield Unit
    case UntypedLatte.Class(name, base, insidesU) => for {
      _ <- subclass(name, base)
      _ <- parseClassStructure(name, insidesU)
    } yield None
  }

  def runWithSeparateStates(defs: List[UntypedLatte.TopDefinition],
                            state: KS): List[CompileException] \/ List[TypedLatte.TopDefinition] = defs match {
    case Nil => \/-(Nil)
    case (hM :: tM) => for {
      tail <- runWithSeparateStates(tM, state)
      hO <- topDefinition(hM).run(state)._2
    } yield hO match {
      case Some(h) => h :: tail
      case None => tail
    }
  }

  def getTypeInformation: TypeEnvironment[TypeInformation] =
    stateToEither(get[KS] map (_._2))

  def emptyState: KS = (Binds(Map(), Map()), TypeInformation.empty)

  override def compile(code: UntypedLatte.Code): Either[List[CompileException], TypedLatte.Code] = {
    val typePhase: TypeEnvironment[TypedLatte.Code] = for {
      _ <- mapM(code._1.toList, addTopDefinition)
      s <- get[KS]: TypeEnvironment[KS]
      typedCode <- EitherT[S, List[CompileException], List[TypedLatte.TopDefinition]](state(runWithSeparateStates(code._1.toList, s)))
      typeInformation <- getTypeInformation
    } yield (typedCode, typeInformation)

    typePhase.run(emptyState)._2.toEither
  }
}
