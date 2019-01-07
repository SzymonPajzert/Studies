package compiler

import language.LLVM.RegisterT
import language.Latte.TypeInformation
import language.Type._
import language.{LLVM, Latte, Type}

import scalaz.Scalaz._
import scalaz.{State, _}

object LatteToQuadCode extends Compiler[Latte.Code, LLVM.Code] {
  // TODO remove globalCode section
  case class CompilationState(globalCode: String = "",
                              tmpCounter: Int = 0,
                              currentSubstitutions: Registers = Map(),
                              functionTypes: Functions,
                              typeInformation: TypeInformation,
                              code: Vector[LLVM.CodeBlock] = Vector(),
                              lastJumpPoint: LLVM.JumpPoint = null)

  type Functions = Map[String, FunctionType]
  type Registers = Map[String, LLVM.RegisterT]
  type StateOf[T] = State[CompilationState, T]
  type StateC = StateOf[List[LLVM.Instruction]]

  def increaseCounter: StateOf[Int] = for {
    state <- get[CompilationState]
    tmpCounter = state.tmpCounter
    _ <- put (state.copy(tmpCounter = tmpCounter + 1))
  } yield tmpCounter

  def getConst(t : Type = IntType): StateOf[LLVM.Register[t.type]] = for {
    counter <- increaseCounter
  } yield LLVM.constRegister(s"tmp$counter", t)

  def allocate(identifier: String, t: Type): StateOf[Unit] = for {
    register <- getRegister(PointerType(transformType(t)))
    _ <- putLine(LLVM.Assign(register, LLVM.alloca(t)))
    state <- get[CompilationState]
    _ <- put (state.copy(currentSubstitutions = state.currentSubstitutions + (identifier -> register)))
  } yield Unit

  def getLocation(identifier: String): StateOf[LLVM.RegisterT] = for {
    compilationState <- get[CompilationState]
    locationMaybe <- gets[CompilationState, Option[LLVM.RegisterT]] (_.currentSubstitutions.get(identifier))

    location: LLVM.RegisterT = locationMaybe match {
      case None => throw new Exception(s"No variable: $identifier in $compilationState")
      case Some(loc) => loc
    }

  } yield location

  def getRegister(t : Type = IntType): StateOf[LLVM.Register[t.type]] = for {
    tmpCounter <- increaseCounter
  } yield LLVM.register(s"tmp$tmpCounter", t)


  /** Creates in global code const with that value and returns pointer to its value
    *
    * @param value
    * @return
    */
  def createStringConst(value: String): StateOf[LLVM.Register[PointerType]] = for {
    constName <- getConst(PointerType(CharType))
    arrayType = ConstArrayType(CharType, value.length + 1)
    newLine = s"""${constName.name} = private unnamed_addr constant ${arrayType.llvmRepr} c"$value\\00""""
    _ <- modify[CompilationState] (state => state.copy(globalCode = state.globalCode + "\n" + newLine))
    register <- getRegister(PointerType(CharType))
    _ <- putLine(LLVM.Assign(register, LLVM.getElementPtr(arrayType, constName)))
  } yield register

  def transformType(value: Type): Type = value match {
    case IntType => IntType
    case VoidType => VoidType
    case CharType => CharType
    case BoolType => BoolType
    case a: PointerType => a
    case StringType => PointerType(CharType)
    case ArrayType(t) => PointerType(transformType(t))
    case ConstArrayType(t, _) => PointerType(transformType(t))
    case AggregateType(name, elts) => AggregateType(name, elts map transformType)
  }

  def isPrimitiveName(location: Latte.FunLocation): Boolean = location match {
    case Latte.FunName(name) => TypePhase.primitiveFunctions contains name
    case Latte.VTableLookup(_, _) => false
  }

  /**
    * Load value stored at the register
    * @param valueLocation Register of type (T*)
    * @return Register of type (T) with value loaded
    */
  def loadRegister(valueLocation: LLVM.RegisterT): StateOf[LLVM.RegisterT] = {
    for {
      tmpRegister <- getRegister(transformType(valueLocation.typeId.deref))
      _ <- putLine(LLVM.Assign(tmpRegister, LLVM.load(valueLocation)))
    } yield tmpRegister
  }


    /**
      * Given array access, calculate address where it is stored
      * @param arrayAccess Expression representing array access
      * @return Register of type (T*)
      */
    def calculateArrayAddress(arrayAccess: Latte.ArrayAccess): StateOf[LLVM.RegisterT] = {
      arrayAccess match {
        case Latte.ArrayAccess(Latte.Variable(array), element) =>
          for {
            arrayLocation <- getLocation(array)      // t**
            arrayPtr <- loadRegister(arrayLocation)  // t*
            valueLocation <- getRegister(transformType(arrayLocation.typeId.deref))

            index <- compileExpression(element)
            _ <- putLine(
              LLVM.Assign(
                valueLocation,
                LLVM.getElementPtr(transformType(arrayPtr.typeId.deref), arrayPtr, List(index))))
          } yield valueLocation
      }
    }

    def lookupTypeStructure(c: ClassType): StateOf[AggregateType] = for {
      typeInformation <- gets[CompilationState, TypeInformation](_.typeInformation)
    } yield AggregateType(c.name, typeInformation.fieldTypes(c))

    /**
    * Given field access, calculate address where it is stored
    * @param expression being aggregate containing given address
    * @return Register of type (T*)
    */
    def calculateFieldAddress(expression: LLVM.RegisterT, i: Int): StateOf[LLVM.RegisterT] = (for {
      tStructure <- (expression.typeId match {
        case PointerType(c: ClassType) => lookupTypeStructure(c)
        case PointerType(tStr: AggregateType) => state(tStr)
      }) : StateOf[AggregateType]

      elements = tStructure.asInstanceOf[AggregateType].elements
      elementType = elements(i)
      registerType = PointerType(elementType)

      valueLocation <- getRegister(registerType)

      _ <- putLine(
        LLVM.Assign(
          valueLocation,
          LLVM.getElementPtr(expression.typeId.deref, expression, List(0, i), Some(expression.typeId))))
    } yield valueLocation) : StateOf[LLVM.RegisterT]

    def compileExpression(expression: Latte.Expression): StateOf[LLVM.Expression] = {
      expression match {
        case Latte.Void => for (r <- getRegister(IntType)) yield r
        case Latte.ConstValue(value) if expression.getType == StringType => {
          val string = value
          for {
            register <- createStringConst(string.asInstanceOf[String])
          } yield register
        }

        case Latte.ConstValue(value) if expression.getType == BoolType => {
          val typeT = transformType(expression.getType)
          state(LLVM.Value(if (value.asInstanceOf[Boolean]) "1" else "0", typeT))
        }

        case Latte.ConstValue(value) => {
          val typeT = transformType(expression.getType)
          state(LLVM.Value(value.toString, typeT))
        }

        case arrayAccess @ Latte.ArrayAccess(Latte.Variable(_), _) => for {
          valueLocation <- calculateArrayAddress(arrayAccess)
          value <- loadRegister(valueLocation)
        } yield value

        case Latte.FieldAccess(expr, i) => for {
          exprRegister <- compileExpression(expr)
          valueLocation <- calculateFieldAddress(exprRegister.asInstanceOf[RegisterT], i)
          value <- loadRegister(valueLocation)
        } yield value

        case Latte.Variable(identifier) => for {
          valueLocation <- getLocation(identifier)
          returnRegister <- loadRegister(valueLocation)
        } yield returnRegister

        case Latte.FunctionCall(Latte.FunName("bool_or"), Seq(left, right)) => for {
          header <- createJumpPoint
          leftFalse <- createJumpPoint
          finished <- createJumpPoint

          _ <- putLine(LLVM.jump(header))
          _ <- putJumpPoint(header)

          leftValue <- compileExpression(left)
          leftValueLabel <- gets[CompilationState, LLVM.JumpPoint](_.lastJumpPoint)
          _ <- putLine(LLVM.JumpIf(leftValue, finished, leftFalse))

          _ <- putJumpPoint(leftFalse)
          rightValue <- compileExpression(right)
          rightValueLabel <- gets[CompilationState, LLVM.JumpPoint](_.lastJumpPoint)
          _ <- putLine(LLVM.jump(finished))

          _ <- putJumpPoint(finished)
          returnRegister <- getRegister(leftValue.typeId)
          _ <- putLine(LLVM.Assign(returnRegister, LLVM.phi(
            leftValueLabel -> leftValue,
            rightValueLabel -> rightValue)))
        } yield returnRegister

        case Latte.FunctionCall(Latte.FunName("bool_and"), Seq(left, right)) => for {
          header <- createJumpPoint
          leftTrue <- createJumpPoint
          finished <- createJumpPoint

          _ <- putLine(LLVM.jump(header))
          _ <- putJumpPoint(header)

          leftValue <- compileExpression(left)
          leftValueLabel <- gets[CompilationState, LLVM.JumpPoint](_.lastJumpPoint)
          _ <- putLine(LLVM.JumpIf(leftValue, leftTrue, finished))
          _ <- putJumpPoint(leftTrue)

          rightValue <- compileExpression(right)
          rightValueLabel <- gets[CompilationState, LLVM.JumpPoint](_.lastJumpPoint)
          _ <- putLine(LLVM.jump(finished))
          _ <- putJumpPoint(finished)
          returnRegister <- getRegister(leftValue.typeId)
          _ <- putLine(LLVM.Assign(returnRegister, LLVM.phi(
            leftValueLabel -> leftValue,
            rightValueLabel -> rightValue)))
        } yield returnRegister

        case Latte.FunctionCall(primitiveName, arguments) if isPrimitiveName(primitiveName) => {
          val Seq(left, right) = arguments

          for {
            leftValue <- compileExpression(left)
            rightValue <- compileExpression(right)

            (operation, retType) = primitiveName match {
              case Latte.FunName("int_add") => (s"add i32", IntType)
              case Latte.FunName("int_mul") => (s"mul i32", IntType)
              case Latte.FunName("int_div") => (s"sdiv i32", IntType)
              case Latte.FunName("int_sub") => (s"sub i32", IntType)
              case Latte.FunName("int_mod") => (s"srem i32", IntType)

              case Latte.FunName("gen_neq") => (s"icmp ne ${leftValue.typeId.llvmRepr}", BoolType)
              case Latte.FunName("gen_gt") => (s"icmp sgt ${leftValue.typeId.llvmRepr}", BoolType)
              case Latte.FunName("gen_eq") => (s"icmp eq ${leftValue.typeId.llvmRepr}", BoolType)
              case Latte.FunName("gen_lt") => (s"icmp slt ${leftValue.typeId.llvmRepr}", BoolType)
              case Latte.FunName("gen_le") => (s"icmp sle ${leftValue.typeId.llvmRepr}", BoolType)
              case Latte.FunName("bool_and") => (s"and ${leftValue.typeId.llvmRepr}", BoolType)
            }

            returnRegister <- getRegister(retType)
            _ <- putLine(LLVM.Assign(returnRegister, new LLVM.Func[Nothing]{
              def getLine: String = s"$operation ${leftValue.name}, ${rightValue.name}"
            }))
          } yield returnRegister
        }

        case Latte.FunctionCall(Latte.FunName("bool_not"), args) => for {
          argVal <- compileExpression(args.head)
          returnRegister <- getRegister(argVal.typeId)

          _ <- putLine(LLVM.Assign(returnRegister, new LLVM.Func[Nothing]{
            def getLine: String = s"xor ${argVal.typeId.llvmRepr} ${argVal.name}, 1"
          }))
        } yield returnRegister

        case Latte.FunctionCall(Latte.FunName(name), arguments) =>
          for {
            functions <- gets[CompilationState, Functions](_.functionTypes)

            identifier = name match {
              case "printInt" => LLVM.FunctionId(name, VoidType)
              case "printString" => LLVM.FunctionId(name, VoidType)
              case definedName if functions contains definedName => {
                LLVM.FunctionId(definedName, transformType(functions(definedName).returnType))
              }
            }

            returnRegister <- getRegister(identifier.returnType)
            argsRegisters <- arguments.toList.traverseS(compileExpression)
            _ <- putLine(LLVM.AssignFuncall(returnRegister, identifier, argsRegisters))
          } yield returnRegister: LLVM.Expression

        case Latte.ArrayCreation(elementType, sizeExpr) => for {
          returnRegister <- getRegister(PointerType(transformType(elementType)))
          size <- compileExpression(sizeExpr)
          _ <- putLine(LLVM.Assign(returnRegister, LLVM.alloca(transformType(elementType), Some(size))))
        } yield returnRegister

        case Latte.InstanceCreation(instanceType: AggregateType) => for {
          returnRegister <- getRegister(PointerType(instanceType))
          _ <- putLine(LLVM.Assign(returnRegister, LLVM.alloca(transformType(instanceType))))
        } yield returnRegister
      }
    }

    def createJumpPoint: StateOf[LLVM.JumpPoint] = for {
      counter <- increaseCounter
    } yield LLVM.JumpPoint(s"label$counter")

    def putLine(line: LLVM.Instruction): StateOf[Unit] = for {
      state <- get[CompilationState]
      _ <- put (state.copy(code = state.code :+ LLVM.Subblock(Vector(line))))
    } yield Unit

    def putJumpPoint(jumpPoint: LLVM.JumpPoint): StateOf[Unit] = for {
      state <- get[CompilationState]
      _ <- modify[CompilationState](x => x.copy(lastJumpPoint = jumpPoint))
      _ <- put (state.copy(
        code = state.code :+ jumpPoint,
        lastJumpPoint = jumpPoint
      ))
    } yield Unit

    /**
      * Given register to store the value, calculate expression and store it there
      * @param location Location to store the value
      * @param value Expression to be calculated
      * @return State of changed source code
      */
    def calculateExpressionIntoRegister(location: LLVM.RegisterT, value: Latte.Expression): StateOf[Unit] = for {
      expression <- compileExpression(value)
      _ <- putLine(LLVM.Literal(s"store ${expression.typeId.llvmRepr} ${expression.name}, ${location.typeId.llvmRepr} ${location.name}"))
    } yield Unit

    def addOneLine(instruction: Latte.Instruction): StateOf[Unit] = {
      instruction match {
        case Latte.Declaration(identifier, typeId) => allocate(identifier, transformType(typeId))
        case Latte.Assignment(Latte.Variable(identifier), value) => for {
          location <- getLocation(identifier)
          _ <- calculateExpressionIntoRegister(location, value)
        } yield Unit
        case Latte.Assignment(arrayAccess: Latte.ArrayAccess, value) => for {
          location <- calculateArrayAddress(arrayAccess)
          _ <- calculateExpressionIntoRegister(location, value)
        } yield Unit
        case Latte.Assignment(Latte.FieldAccess(expr, offset), value) => for {
          expression <- compileExpression(expr)
          location <- calculateFieldAddress(expression.asInstanceOf[RegisterT], offset)
          _ <- calculateExpressionIntoRegister(location, value)
        } yield Unit
        case Latte.DiscardValue(value) => for {
          _ <- compileExpression(value)
        } yield Unit
        case Latte.Return(valueUncompiled) => for {
          value <- (valueUncompiled match {
            case None => state(None)
            case Some(v) => compileExpression(v) map (Some(_))
          }) : StateOf[Option[LLVM.Expression]]
          _ <- putLine(LLVM.Return(value))
        } yield Unit
        case Latte.While(condition, instructions) => for {
          beginning <- createJumpPoint
          ifTrue <- createJumpPoint
          ifFalse <- createJumpPoint

          _ <- putLine(LLVM.jump(beginning))
          _ <- putJumpPoint(beginning)
          expressionRegister <- compileExpression(condition)
          _ <- putLine(LLVM.JumpIf(expressionRegister, ifTrue, ifFalse))
          _ <- putJumpPoint(ifTrue)
          _ <- addOneLine(instructions)
          _ <- putLine(LLVM.jump(beginning))
          _ <- putJumpPoint(ifFalse)
        } yield Unit
        case Latte.IfThen(condition, thenInst, Some(elseInst)) => for {
          ifTrue <- createJumpPoint
          ifFalse <- createJumpPoint
          after <- createJumpPoint

          expressionRegister <- compileExpression(condition)
          _ <- putLine(LLVM.JumpIf(expressionRegister, ifTrue, ifFalse))
          _ <- putJumpPoint(ifTrue)
          _ <- addOneLine(thenInst)
          _ <- putLine(LLVM.jump(after))
          _ <- putJumpPoint(ifFalse)
          _ <- addOneLine(elseInst)
          _ <- putLine(LLVM.jump(after))
          _ <- putJumpPoint(after)
        } yield Unit
        case Latte.IfThen(condition, thenInst, None) => for {
          ifTrue <- createJumpPoint
          after <- createJumpPoint

          expressionRegister <- compileExpression(condition)
          _ <- putLine(LLVM.JumpIf(expressionRegister, ifTrue, after))
          _ <- putJumpPoint(ifTrue)
          _ <- addOneLine(thenInst)
          _ <- putLine(LLVM.jump(after))
          _ <- putJumpPoint(after)
        } yield Unit
        case Latte.BlockInstruction(instructions) => for {
          _ <- addManyLines(instructions)
        } yield Unit
      }
    }

    def emptyState: StateOf[Unit] = State[CompilationState, Unit]{ x => (x, ())}

    def addManyLines(instructions: List[Latte.Instruction]): StateOf[Unit] = {
      for {
        _ <- instructions.traverseS[CompilationState, Unit](addOneLine)
      } yield Unit
    }

    def putSignature(signatureArgs: List[(String, Type)]): StateOf[Unit] = signatureArgs match {
      case Nil => state(Unit)
      case (identifier, t) :: rest => for {
        _ <- allocate(identifier, transformType(t))
        argRegister <- getLocation(identifier)
        _ <- putLine(LLVM.Literal(s"store ${argRegister.typeId.deref.llvmRepr} %$identifier, ${argRegister.typeId.llvmRepr} ${argRegister.name}"))
        _ <- putSignature(rest)
      } yield Unit
    }

  override def compile(code: Latte.Code): Either[List[CompileException], LLVM.Code] = {
    val signatures = (code.definitions map (func => {
      func.signature.identifier -> FunctionType(func.signature.returnType, func.signature.arguments map (_._2))
    })).toMap

    val mapFunction: Latte.Func => (String, LLVM.Block) = {
      case (Latte.Func(signature, codeBlock)) => {
        val codeCalculation: StateOf[CompilationState] = for {
          _ <- putSignature(signature.arguments)
          _ <- addManyLines(codeBlock)
          s <- get
        } yield s

        val compiledFunction = codeCalculation(CompilationState(
          typeInformation = code.typeInformation,
          functionTypes = signatures))._2

        (compiledFunction.globalCode,
          LLVM.Block(LLVM.FunctionId(signature.identifier, transformType(signature.returnType)),
            signature.arguments map { case (name, t) => LLVM.register(name, transformType(t)) },
            compiledFunction.code))
      }
    }

    val (globalsFromFunctions, blocks) = (code.definitions map mapFunction).unzip
    val globals = globalsFromFunctions.mkString("\n") + "\n" + code.globalLLVM

    Right(LLVM.Code(globals, blocks.toList))
  }
}
