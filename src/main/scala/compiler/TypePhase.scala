package compiler

import language.Type._
import language.{ParsedClasses, Type, TypeInformation, TypedLatte}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

/**
  * Performs following checks:
  *   - static binding of variables
  *   - renaming hiding variables
  *   - typing the expressions
  */
object TypePhase extends Compiler[ParsedClasses.Code, TypedLatte.Code] {
  case class VariableState(counter: Int, fromCurrentBlock: Boolean, typeId: Type)
  type VarBinds = Map[String, VariableState]
  type FunBinds = Map[String, Type]

  def putNewFunBind(name: String, typeId: Type) : TypeEnvironment[Unit] = for {
    funBinds <- gets[KS, FunBinds](_._1.functions) : TypeEnvironment[FunBinds]
    _ <- (funBinds get name) match {
      case Some(_) => createError(s"$name already defined")
      case None => modifyBinds { binds => binds.copy(
        functions = binds.functions + (name -> typeId))}
    }
  } yield Unit

  case class Binds(variables: VarBinds, functions: FunBinds)

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

  def location(loc: ParsedClasses.LocationInf): TypeEnvironment[TypedLatte.LocationInf] = loc._1 match {
    case ParsedClasses.Variable(ident) => for {
      binds <- getBinds
      (identifier, typeId) <- (binds.variables get ident match {
        case Some(state) => (ident + state.counter.toString, state.typeId)
        case None => createError(s"Undefined variable $ident")
      }): TypeEnvironment[(String, Type)]
    } yield (TypedLatte.Variable(identifier), typeId)

    case ParsedClasses.ArrayAccess(arrayU, elementU) => for {
      array <- expression(arrayU)
      element <- expression(elementU)
    } yield (TypedLatte.ArrayAccess(array, element), array._2.asInstanceOf[ArrayType].eltType)

    case ParsedClasses.FieldAccess(placeU, element) => for {
      place <- expression(placeU)
      typeInformation <- getTypeInformation
      elementType <- (place._2 match {
        case PointerType(c @ ClassType(_)) => typeInformation.field(c).findType(element) match {
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

  def primitiveFunctions: Map[String, Type] = Map (
    "int_add" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_sub" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_div" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_mul" -> FunctionType (IntType, Seq (IntType, IntType)),
    "gen_neq" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_gt" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_eq" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_lt" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_le" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "bool_or" -> FunctionType (BoolType, Seq (BoolType, BoolType)),
    "bool_and" -> FunctionType (BoolType, Seq (BoolType, BoolType)),
    "int_mod" -> FunctionType (IntType, Seq(IntType, IntType))
  )

  def funLocation(fLoc: ParsedClasses.FunLocation): TypeEnvironment[TypedLatte.FunLocationInf] = fLoc match {
    case ParsedClasses.FunName(name: String) => for {
      functionType <- (name match {
        case "printInt" => FunctionType(VoidType, Seq(IntType))
        case "printString" => FunctionType(VoidType, Seq(StringType))
        case _ if primitiveFunctions contains name => primitiveFunctions(name)
        case "bool_not" => FunctionType(BoolType, Seq(BoolType))
        case userDefinedName => lookupFunctionSignature(userDefinedName)
      }): TypeEnvironment[Type]
    } yield (TypedLatte.FunName(name), functionType)

    case vtable@ParsedClasses.VTableLookup(exprU, ident) => for {
      expr <- expression(exprU)
      typeInformation <- getTypeInformation

      methodType <- (expr._2 match {
        case PointerType(expressionType: ClassType) =>
          typeInformation.method(expressionType)
            .findType(ident)
            .map(ok)
            .getOrElse(createError(s"$expressionType has no field $ident"): TypeEnvironment[FunctionType])

        case _ => createError(s"Wrong type: $vtable")
      }): TypeEnvironment[FunctionType]
    } yield (TypedLatte.VTableLookup(expr, ident), methodType): TypedLatte.FunLocationInf
  }

  def extractType(value: Any): Type = value match {
    case _ if value.isInstanceOf[Int] => Type.IntType
    case _ if value.isInstanceOf[Boolean] => Type.BoolType
    case _ if value.isInstanceOf[String] => Type.StringType
  }


  def returnType(functionLoc: TypedLatte.FunLocationInf): TypeEnvironment[Type] = functionLoc._2 match {
    case FunctionType(rT, _) => rT
    case _ => createError(s"${functionLoc._1} is not callable")
  }

  type ExprMod = TypedLatte.ExpressionInf => TypedLatte.ExpressionInf

  def checkTypeWithImplicitCasts(inf: TypedLatte.ExpressionInf, expectedType: Type): TypeEnvironment[TypedLatte.ExpressionInf] = for {
    typeInformation <- getTypeInformation

    result <- (inf._2, expectedType) match {
      case (a, b) if a == b => ok(inf)

      case (PointerType(a: ClassType), PointerType(b: ClassType)) if typeInformation.isParent(a, b) =>
        ok((TypedLatte.Cast(PointerType(b), inf), PointerType(b)))

      case _ => createError(s"Wrong type. Expected: $expectedType instead ${inf._2}")
    }
  } yield result

  def expression(expr: ParsedClasses.ExpressionInf): TypeEnvironment[TypedLatte.ExpressionInf] = expr._1 match {
    case ParsedClasses.FunctionCall(locU , arguments) => for {
      loc <- funLocation(locU._1)
      args <- mapM(arguments.toList, expression)
      rt <- returnType(loc)

      // TODO check args match
    } yield (TypedLatte.FunctionCall(loc, args), rt)

    case ParsedClasses.ConstValue(value) => (TypedLatte.ConstValue(value), extractType(value))

    case ParsedClasses.ArrayCreation(elementType, size) => for {
      typedSizeQ <- expression(size)
      typedSize <- checkTypeWithImplicitCasts(typedSizeQ, IntType)
      arrayType = ArrayType(elementType)
    } yield (TypedLatte.ArrayCreation(elementType, typedSize), arrayType)

    case ParsedClasses.Cast(castType, expressionInfU) => for {
      expressionInf <- expression(expressionInfU)
    } yield (expressionInf._1, castType)

    case ParsedClasses.Null(c) => (TypedLatte.Null(c), PointerType(c))

    case ParsedClasses.InstanceCreation(typeT) => (TypedLatte.InstanceCreation(typeT), typeT)

    case loc: ParsedClasses.Location => location((loc, Unit))
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

  def instruction(ins: ParsedClasses.Instruction): TypeEnvironment[TypedLatte.Instruction] = ins match {
    case ParsedClasses.Declaration(identifier, typeValue) => for {
      binds <- getBinds

      transType = typeValue match {
        case c: ClassType => PointerType(c)
        case t => t
      }

      counter <- (binds.variables get identifier match {
        case Some(state) if state.fromCurrentBlock => createError(s"$identifier already defined")
        case Some(state) => setCounter(identifier, state.counter + 1, transType)
        case None => setCounter(identifier, 0, transType)
        }) : TypeEnvironment[Int]
    } yield TypedLatte.Declaration(identifier + counter.toString , typeValue): TypedLatte.Instruction

    case ParsedClasses.Assignment(locU, exprU) => for {
      loc <- location(locU)
      exprQ <- expression(exprU)
      expr <- checkTypeWithImplicitCasts(exprQ, loc._2)
    } yield TypedLatte.Assignment(loc, expr)

    case ParsedClasses.BlockInstruction(blockU) => (for {
      s <- get[KS]: TypeEnvironment[KS]
      blockE = mapM(blockU, instruction).run(markBindsAsOld(s))._2
      block <- EitherT[S, List[CompileException], List[TypedLatte.Instruction]](state(blockE)): TypeEnvironment[List[TypedLatte.Instruction]]
    } yield TypedLatte.BlockInstruction(block)): TypeEnvironment[TypedLatte.Instruction]

    case ParsedClasses.DiscardValue(expr) => expression(expr) map TypedLatte.DiscardValue

    case ParsedClasses.Return(value) => value match {
      case None => TypedLatte.Return(None)
      case Some(v) => expression(v) map (vE => TypedLatte.Return(Some(vE)))
    }

    case ParsedClasses.IfThen(conditionU, thenInstU, elseUOpt) => for {
      condition <- expression(conditionU)
      thenInst <- instruction(thenInstU)
      elseOpt <- (elseUOpt match {
        case None => None
        case Some(els) => instruction(els) map (Some(_))
      }): TypeEnvironment[Option[TypedLatte.Instruction]]
    } yield condition._1 match {
      case TypedLatte.ConstValue(true) => thenInst
      case TypedLatte.ConstValue(false) => TypedLatte.BlockInstruction(elseOpt.toList)
      case _ => TypedLatte.IfThen(condition, thenInst, elseOpt)
    }

    case ParsedClasses.While(conditionU, instrU) => for {
      condition <- expression(conditionU)
      instr <- instruction(instrU)
    } yield TypedLatte.While(condition, instr)
  }

  def addReturn(instructions: TypedLatte.Block, returnType: Type.Type): TypedLatte.Block = {
    if(instructions.isEmpty || !instructions.last.isInstanceOf[TypedLatte.Return]) {
      val returnInst = returnType match {
        case VoidType => TypedLatte.Return(None)
        case IntType => TypedLatte.Return(Some((TypedLatte.ConstValue(0), returnType)))
        case PointerType(_) => TypedLatte.Return(Some((TypedLatte.Null(returnType.deref.asInstanceOf[ClassType]), returnType)))
      }
      instructions ::: List(returnInst)
    } else instructions
  }

  def toplevelFunc: ParsedClasses.Func => TypeEnvironment[TypedLatte.Func] = {
    case ParsedClasses.Func(signature, codeU) => {
      val definitions: List[ParsedClasses.Instruction] =
        signature.arguments map { case (name, value) => ParsedClasses.Declaration(name, value): ParsedClasses.Instruction }

      for {
        _ <- mapM(definitions, instruction)
        codeWithoutReturn <- mapM(codeU, instruction)
        code = addReturn(codeWithoutReturn, signature.returnType)
      } yield {
        val sig = signature.asInstanceOf[TypedLatte.FunctionSignature]
        val args = sig.arguments map {case (name, t) => (name + "0", t)}
        TypedLatte.Func(sig.copy(arguments = args), code)
      }
    }
  }

  def topDefinition: ParsedClasses.TopDefinition => TypeEnvironment[Option[TypedLatte.TopDefinition]] = {
    case func : ParsedClasses.Func => for {
      f <- toplevelFunc(func)
    } yield Some(f)
    case _ => ok(None)
  }

  /**
    * Add signature of the function to the environment or fail
    * @param signature
    * @return
    */
  def addSignature(signature: ParsedClasses.FunctionSignature): TypeEnvironment[Unit] = {
    val name = signature.identifier
    val typeId = FunctionType(signature.returnType, signature.arguments.map(_._2))
    for {
      binds <- getBinds: TypeEnvironment[Binds]
      _ <- putNewFunBind(name, typeId) : TypeEnvironment[Unit]
    } yield Unit
  }

  /**
    * Parse top level definitions and return methods that were hidden inside class
    * @return
    */
  def addTopDefinition: ParsedClasses.TopDefinition => TypeEnvironment[Unit] = {
    case ParsedClasses.Func(signature, _) => for (_ <- addSignature(signature)) yield Unit
  }

  def runWithSeparateStates(defs: List[ParsedClasses.TopDefinition],
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

  def initialState(tI: TypeInformation): KS = (Binds(Map(), Map()), tI)

  override def compile(code: ParsedClasses.Code): Either[List[CompileException], TypedLatte.Code] = {
    val typePhase: TypeEnvironment[TypedLatte.Code] = for {
      _ <- mapM(code._1.toList, addTopDefinition)

      s <- get[KS]: TypeEnvironment[KS]
      typedCodeT = runWithSeparateStates(code._1.toList, s)

      typedCode <- EitherT[S, List[CompileException], List[TypedLatte.TopDefinition]](state(typedCodeT))
      typeInformation <- getTypeInformation
    } yield (typedCode, typeInformation)

    typePhase.run(initialState(code._2))._2.toEither
  }
}
