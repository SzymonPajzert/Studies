package compiler

import language._
import language.Type._

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
  def undefinedFunction(name: String): TypeEnvironment[TypingFailure] =
    getTypeInformation map (UndefinedFunction(name, _))
  def undefinedVariable(name: String): TypeEnvironment[TypingFailure] =
    getTypeInformation map (UndefinedVariable(name, _))
  def duplicateDefinition(name: String): TypeEnvironment[TypingFailure] =
    getTypeInformation map (DuplicateDefinition(name, _))
  def fieldNotFound(name: String, classT: ClassType, expr: String = "<expr>"): TypeEnvironment[TypingFailure]  =
    getTypeInformation map (FieldNotFound(name, classT, expr, _))
  def methodNotFound(name: String, classT: ClassType, expr: String = "<expr>"): TypeEnvironment[TypingFailure]  =
    getTypeInformation map (MethodNotFound(name, classT, expr, _))
  def wrongType(expected: Type, actual: Type, expr: String = "<expr>"): TypeEnvironment[TypingFailure]  =
    getTypeInformation map (WrongType(expected, actual, expr, _))
  def wrongArgumentNumber(expected: Int, actual: Int, name: String): TypeEnvironment[TypingFailure] =
    getTypeInformation map (WrongArgumentNumber(expected, actual, name, _))
  def classUndefined(className: ClassType): TypeEnvironment[TypingFailure] =
    getTypeInformation map (ClassUndefined(className, _))
  def functionVoidArgument(funName: String, argName: String): TypeEnvironment[TypingFailure] =
    getTypeInformation map (FunctionVoidArgument(funName, argName, _))
  def wrongReturnType(expected: Type, instead: Type): TypeEnvironment[TypingFailure] =
    getTypeInformation map (WrongReturnType(expected, instead, _))

  case class VariableState(counter: Int, fromCurrentBlock: Boolean, typeId: Type)
  type VarBinds = Map[String, VariableState]
  type FunBinds = Map[String, Type]

  def putNewFunBind(name: String, typeId: Type) : TypeEnvironment[Unit] = for {
    funBinds <- gets[KS, FunBinds](_._1.functions) : TypeEnvironment[FunBinds]
    _ <- funBinds get name match {
      case Some(_) => createError(duplicateDefinition(name))
      case None => modifyBinds { binds => binds.copy(
        functions = binds.functions + (name -> typeId))}
    }
  } yield Unit

  case class Binds(variables: VarBinds, functions: FunBinds)

  type KS = (Binds, TypeInformation, Type)

  type S[A] = State[KS, A]
  type TypeEnvironment[A] = EitherT[S, CompileException, A]

  implicit def ok[A](value: A): TypeEnvironment[A] =
    EitherT[S, CompileException, A](state[KS, CompileException \/ A](\/-(value)))

  implicit def stateToEither[A](value: State[KS, A]): TypeEnvironment[A] = {
    EitherT[S, CompileException, A](value map (\/-(_)))
  }

  def createError[A](typingFailure: TypeEnvironment[TypingFailure]): TypeEnvironment[A] = for {
      failure <- typingFailure
      result <- EitherT[S, CompileException, A](state(-\/(failure)))
    } yield result

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
      pair <- (binds.variables get ident match {
        case Some(state) => (ident + state.counter.toString, state.typeId)
        case None => createError(undefinedVariable(ident))
      }): TypeEnvironment[(String, Type)]
      (identifier, typeId) = pair

    } yield (TypedLatte.Variable(identifier), typeId)

    case ParsedClasses.ArrayAccess(arrayU, elementU) => for {
      array <- expression(arrayU)
      element <- expression(elementU)
    } yield (TypedLatte.ArrayAccess(array, element), array._2.deref.asInstanceOf[ArrayType].eltType)

    case ParsedClasses.FieldAccess(placeU, element) => for {
      place <- expression(placeU)
      typeInformation <- getTypeInformation
      result <- (place._2, element) match {
        case (PointerType(c@ClassType(_)), _) => typeInformation.field(c).findType(element) match {
          case Some(t) => ok((TypedLatte.FieldAccess(place, element), t))
          case None => createError(fieldNotFound(element, c))
        }
        case (t, _) => createError(wrongType(PointerType(ClassType("class")), t))
      }
    } yield result
  }

  def lookupFunctionSignature(functionName: String): TypeEnvironment[Type] = for {
    signatures <- getBinds

    foundValue <- (signatures.functions get functionName match {
      case Some(value) => ok(value)
      case None => createError(undefinedFunction(functionName))
    }) : TypeEnvironment[Type]
  } yield foundValue

  def primitiveFunctions: Map[String, Type] = Map (
    "int_add" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_sub" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_div" -> FunctionType (IntType, Seq (IntType, IntType)),
    "int_mul" -> FunctionType (IntType, Seq (IntType, IntType)),
    "gen_neq" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_ge" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_gt" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_eq" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_lt" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "gen_le" -> FunctionType (BoolType, Seq (IntType, IntType)),
    "bool_or" -> FunctionType (BoolType, Seq (BoolType, BoolType)),
    "bool_and" -> FunctionType (BoolType, Seq (BoolType, BoolType)),
    "int_mod" -> FunctionType (IntType, Seq(IntType, IntType))
  )

  def dropFirstArgument(methodType: FunctionType): FunctionType = {
    methodType.copy(argsType = methodType.argsType.toList.tail)
  }

  def funLocation(fLoc: ParsedClasses.FunLocation): TypeEnvironment[TypedLatte.FunLocationInf] = fLoc match {
    case ParsedClasses.FunName(name: String) => for {
      functionType <- (name match {
        case _ if primitiveFunctions contains name => primitiveFunctions(name)
        case "bool_not" => FunctionType(BoolType, Seq(BoolType))
        case userDefinedName => lookupFunctionSignature(userDefinedName)
      }): TypeEnvironment[Type]
    } yield (TypedLatte.FunName(name), functionType)

    case vtable@ParsedClasses.VTableLookup(exprU, ident) => for {
      expr <- expression(exprU)
      typeInformation <- getTypeInformation

      pair <- expr._2 match {
        case PointerType(expressionType: ClassType) if !typeInformation.contains(expressionType)
          => createError(classUndefined(expressionType))

        case PointerType(expressionType: ClassType) =>
          typeInformation.method(expressionType)
            .find(ident)
            .map(ok)
            .getOrElse(createError(methodNotFound(ident, expressionType)))

        case _ => createError(wrongType(PointerType(VoidType), expr._2))
      }
      (methodType, definedInClass) = pair

      cast: TypedLatte.ExpressionInf = (TypedLatte.Cast(PointerType(definedInClass), expr), PointerType(definedInClass))

    } yield (TypedLatte.VTableLookup(cast, ident), methodType): TypedLatte.FunLocationInf
  }

  def extractType(value: Any): Type = value match {
    case _ if value.isInstanceOf[Int] => Type.IntType
    case _ if value.isInstanceOf[Boolean] => Type.BoolType
    case _ if value.isInstanceOf[String] => Type.StringType
  }


  def functionType(functionLoc: TypedLatte.FunLocationInf): TypeEnvironment[(Type, Seq[Type])] = functionLoc._2 match {
    case FunctionType(rT, argsType) if functionLoc._1.isInstanceOf[TypedLatte.FunName] => (rT, argsType)
    case FunctionType(rT, thisArg :: argsType) if functionLoc._1.isInstanceOf[TypedLatte.VTableLookup] => (rT, argsType)
    case _ => createError(wrongType(FunctionType(VoidType, Seq()), functionLoc._2))
  }

  type ExprMod = TypedLatte.ExpressionInf => TypedLatte.ExpressionInf

  def checkTypeWithImplicitCasts(inf: TypedLatte.ExpressionInf, expectedType: Type): TypeEnvironment[TypedLatte.ExpressionInf] = for {
    typeInformation <- getTypeInformation

    result <- (inf._2, expectedType) match {
      case (a, b) if a == b => ok(inf)

      case (a, b) if typeInformation.isParent(a, b) =>
        ok((TypedLatte.Cast(b, inf), b))

      case _ => createError(wrongType(expectedType, inf._2, inf._1.pretty))
    }
  } yield result

  def checkType(left: Type, right: Type): Boolean = (left, right) match {
    case (l, r) if l == r => true
    case (PointerType(l), PointerType(r)) => checkType(l, r)
    case (PointerType(_), null) => true
    case (null, PointerType(_)) => true
    case _ => false
  }

  def funName: TypedLatte.FunLocationInf => TypeEnvironment[String] = {
    case (TypedLatte.FunName(name), _) => ok(name)
    case (TypedLatte.VTableLookup(_, name), _) => ok(name)
  }

  def expression(expr: ParsedClasses.ExpressionInf): TypeEnvironment[TypedLatte.ExpressionInf] = expr._1 match {
    case ParsedClasses.FunctionCall((ParsedClasses.FunName(name), _), arguments) if Set("gen_eq", "gen_neq") contains name => for {
      argsNotChecked <- mapM(arguments.toList, expression)
      typeInformation <- getTypeInformation

      argPairWithType <- argsNotChecked match {
        case left :: right :: Nil => (left._2, right._2) match {
            case (a, b) if a == b => ok((Seq(left, right), a))

            case (subclass, base) if typeInformation.isParent(subclass, base) => {
              val leftCast = (TypedLatte.Cast(base, left), base)
              ok((Seq(leftCast, right), base))
            }

            case (base, subclass) if typeInformation.isParent(subclass, base) => {
              val rightCast = (TypedLatte.Cast(base, right), base)
              ok((Seq(left, rightCast), base))
            }

            case (a, b) => createError(wrongType(a, b, right._1.pretty))
          }
        case _ => createError(wrongArgumentNumber(2, arguments.length, name))
      }

      args = argPairWithType._1
      argT = argPairWithType._2

      funcT = FunctionType(BoolType, Seq(argT, argT))

    } yield (TypedLatte.FunctionCall((TypedLatte.FunName(name), funcT), args), BoolType)

    case ParsedClasses.FunctionCall((ParsedClasses.FunName("int_add"), _), arguments) => for {
      argsNotChecked <- mapM(arguments.toList, expression)
      pair <- (argsNotChecked match {
        case left :: right :: Nil => (left._2, right._2) match {
          case (StringType, StringType) => ("string_concat", FunctionType(StringType, Seq(StringType, StringType)))
          case (IntType, IntType) => ("int_add", FunctionType(IntType, Seq(IntType, IntType)))
          case _ => createError(wrongType(left._2, right._2))
        }
        case _ => createError(wrongArgumentNumber(2, arguments.length, "int_add"))
      }) : TypeEnvironment[(String, FunctionType)]
      (name, funType) = pair

    } yield (TypedLatte.FunctionCall((TypedLatte.FunName(name), funType), argsNotChecked), funType.returnType)

    case ParsedClasses.FunctionCall(locU , arguments) => for {
      loc <- funLocation(locU._1)
      name <- funName(loc)
      argsNotChecked <- mapM(arguments.toList, expression)
      pair <- functionType(loc)
      (rt, argTypes) = pair

      _ <- if (argTypes.length != argsNotChecked.length)
        createError(wrongArgumentNumber(argTypes.length, argsNotChecked.length, name))
      else ok(Unit)

      args <- mapM(argsNotChecked zip argTypes, (checkTypeWithImplicitCasts _).tupled)

    } yield (TypedLatte.FunctionCall(loc, args), rt)

    case ParsedClasses.ConstValue(value) => (TypedLatte.ConstValue(value), extractType(value))

    case ParsedClasses.ArrayCreation(elementType, size) => for {
      typedSizeQ <- expression(size)
      typedSize <- checkTypeWithImplicitCasts(typedSizeQ, IntType)
      arrayType = PointerType(new ArrayType(elementType))
    } yield (TypedLatte.ArrayCreation(elementType, typedSize), arrayType)

    case ParsedClasses.Cast(castType, expressionInfU) => for {
      expressionInf <- expression(expressionInfU)
    } yield (expressionInf._1, castType)

    case ParsedClasses.Null(c) => (TypedLatte.Null(c), c)

    case ParsedClasses.InstanceCreation(typeT) => (TypedLatte.InstanceCreation(typeT), typeT)

    case loc: ParsedClasses.Location => for {
      res <- location((loc, Unit))
    } yield res
  }

  def markBindsAsOld(state: KS): KS = {
    val binds = state._1

    val newBinds = binds.copy(variables = binds.variables.map {
      pair => (pair._1, pair._2.copy(fromCurrentBlock = false))
    })

    state.copy(_1 = newBinds)
  }

  def setCounter(identifier: String, counter: Int, typeValue: Type): TypeEnvironment[Int] = {
    (for {
      _ <- modifyBinds { binds => binds.copy(
        variables = binds.variables + (identifier -> VariableState(counter, true, typeValue)))
      }
    } yield counter) : TypeEnvironment[Int]
  }

  def functionReturnType: TypeEnvironment[Type] = for {
    triple <- get[KS]
  } yield triple._3

  def instruction(ins: ParsedClasses.Instruction): TypeEnvironment[TypedLatte.Instruction] = ins match {
    case ParsedClasses.Declaration(identifier, typeValue) => for {
      binds <- getBinds

      transType = typeValue match {
        case c: ClassType => PointerType(c)
        case t => t
      }

      counter <- (binds.variables get identifier match {
        case Some(state) if state.fromCurrentBlock => createError(duplicateDefinition(identifier))
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
      block <- EitherT[S, CompileException, List[TypedLatte.Instruction]](state(blockE)): TypeEnvironment[List[TypedLatte.Instruction]]
    } yield TypedLatte.BlockInstruction(block)): TypeEnvironment[TypedLatte.Instruction]

    case ParsedClasses.DiscardValue(expr) => expression(expr) map TypedLatte.DiscardValue

    case ParsedClasses.Return(value) => value match {
      case None => for {
        returnType <- functionReturnType
        _ <- if (returnType == VoidType) ok(Unit) else createError(wrongReturnType(returnType, VoidType))
      } yield TypedLatte.Return(None)

      case Some(v) => for {
        returnType <- functionReturnType
        _ <- if (returnType == VoidType) createError(wrongReturnType(VoidType, returnType)) else ok(Unit)
        exprCast <- expression(v)
        expr <- checkTypeWithImplicitCasts(exprCast, returnType)
      } yield TypedLatte.Return(Some(expr))
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

  def toplevelFunc: ParsedClasses.Func => TypeEnvironment[TypedLatte.Func] = {
    case ParsedClasses.Func(signature, codeU) => {
      val definitions: List[ParsedClasses.Instruction] =
        signature.arguments map { case (name, value) => ParsedClasses.Declaration(name, value): ParsedClasses.Instruction }

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
    signature.arguments find { case (_, t) => t == VoidType} match {
      case Some(voidArgument) => createError(functionVoidArgument(name, voidArgument._1))
      case None => putNewFunBind(name, typeId)
    }
  }

  /**
    * Parse top level definitions and return methods that were hidden inside class
    * @return
    */
  def addTopDefinition: ParsedClasses.TopDefinition => TypeEnvironment[Unit] = {
    case ParsedClasses.Func(signature, _) => for (_ <- addSignature(signature)) yield Unit
  }

  def runWithSeparateStates(defs: List[ParsedClasses.TopDefinition],
                            state: KS): CompileException \/ List[TypedLatte.TopDefinition] = defs match {
    case Nil => \/-(Nil)
    case (hM :: tM) => for {
      tail <- runWithSeparateStates(tM, state)
      hO <- topDefinition(hM).run(state.copy(_3 = hM.asInstanceOf[ParsedClasses.Func].signature.returnType))._2
    } yield hO match {
      case Some(h) => h :: tail
      case None => tail
    }
  }

  def getTypeInformation: TypeEnvironment[TypeInformation] =
    stateToEither(get[KS] map (_._2))

  def initialState(tI: TypeInformation): KS = {
    (Binds(Map(), DeclaredFunctions.all), tI, VoidType)
  }

  override def compile(code: ParsedClasses.Code): Either[CompileException, TypedLatte.Code] = {
    val typePhase: TypeEnvironment[TypedLatte.Code] = for {
      _ <- mapM(code._1.toList, addTopDefinition)

      s <- get[KS]: TypeEnvironment[KS]
      typedCodeT = runWithSeparateStates(code._1.toList, s)

      typedCode <- EitherT[S, CompileException, List[TypedLatte.TopDefinition]](state(typedCodeT))
      typeInformation <- getTypeInformation
    } yield (typedCode, typeInformation)

    typePhase.run(initialState(code._2))._2.toEither
  }
}
