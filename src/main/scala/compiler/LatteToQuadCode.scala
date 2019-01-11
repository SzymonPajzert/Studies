package compiler

import language.LLVM.RegisterT
import language.Type._
import language.{LLVM, Latte, Type, TypeInformation}

import scalaz.Scalaz._
import scalaz.{State, _}


object LatteToQuadCode extends Compiler[Latte.Code, LLVM.Code] {
  // TODO remove globalCode section
  case class CompilationState(globalCode: String = "",
                              tmpCounter: Int = 0,
                              currentSubstitutions: Registers = Map(),
                              functionTypes: Functions,
                              typeInformation: TypeInformation, // TODO remove
                              code: Vector[LLVM.CodeBlock] = Vector(),
                              lastJumpPoint: LLVM.JumpPoint = null)

  type Functions = Map[String, FunctionType]
  type Registers = Map[String, LLVM.RegisterT]
  type StateOf[T] = State[CompilationState, T]
  type StateC = StateOf[List[LLVM.Instruction]]

  def mapM[A, B](list: List[A], f: A => StateOf[B]): StateOf[List[B]] =
    (list foldRight (state(List()): StateOf[List[B]])) { (elt, acc) => for {
      newElt <- f(elt)
      okAcc <- acc
    } yield newElt :: okAcc
  }

  def increaseCounter: StateOf[Int] = for {
    state <- get[CompilationState]
    tmpCounter = state.tmpCounter
    _ <- put (state.copy(tmpCounter = tmpCounter + 1))
  } yield tmpCounter

  def getConst(t : Type = IntType): StateOf[LLVM.Register[t.type]] = for {
    counter <- increaseCounter
  } yield LLVM.constRegister(s"tmp$counter", t)

  def allocate(identifier: String, t: Type): StateOf[Unit] = for {
    eltType <- transformType(t)
    register <- getRegister(PointerType(eltType))
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

  def transformType(value: Type): StateOf[Type] = value match {
    case IntType =>        state(IntType)
    case VoidType =>       state(VoidType)
    case CharType =>       state(CharType)
    case BoolType =>       state(BoolType)
    case a: PointerType => state(a)
    case StringType =>     state(PointerType(CharType))

    case ArrayType(t) =>         transformType(t) map PointerType
    case ConstArrayType(t, _) => transformType(t) map PointerType

    case AggregateType(name, elts) =>
      (elts.toList.traverseS(t => transformType(t)): StateOf[List[Type]]) map (AggregateType(name, _))
    case c: ClassType => gets[CompilationState, Type](_.typeInformation.aggregate(c))
  }

  def isPrimitiveName(location: Latte.FunLocation): Boolean = location match {
    case Latte.FunName(name) => TypePhase.primitiveFunctions contains name
    case Latte.VTableLookup(_, _, _) => false
  }

  /**
    * Load value stored at the register
    * @param valueLocation Register of type (T*)
    * @return Register of type (T) with value loaded
    */
  def loadRegister(valueLocation: LLVM.RegisterT): StateOf[LLVM.RegisterT] = {
    for {
      eltType <- transformType(valueLocation.typeId.deref)
      tmpRegister <- getRegister(eltType)
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
            arrayType <- transformType(arrayLocation.typeId.deref)
            valueLocation <- getRegister(arrayType)

            index <- compileExpression(element)
            _ <- putLine(
              LLVM.Assign(
                valueLocation,
                LLVM.getElementPtr(arrayType.deref, arrayPtr, List(index))))
          } yield valueLocation
      }
    }

    /**
    * Given field access, calculate address where it is stored
    * @param expression being aggregate containing given address
    * @return Register of type (T*)
    */
    def calculateFieldAddress(expression: LLVM.RegisterT, i: Int): StateOf[LLVM.RegisterT] = {
      val exprPointerType: PointerType = expression.typeId.asInstanceOf[PointerType]
      val exprType : ClassType = exprPointerType.deref.asInstanceOf[ClassType]

      for {
        elements <- gets[CompilationState, Seq[Type]](_.typeInformation.aggregate(exprType).elements)
        elementType = elements(i + 1)
        registerType = PointerType(elementType)

        valueLocation <- getRegister(registerType)

        _ <- putLine(
          LLVM.Assign(
            valueLocation,
            LLVM.getElementPtr(expression.typeId.deref, expression, List(0, i + 1))))
      } yield valueLocation
    } : StateOf[LLVM.RegisterT]

    def compileExpression(expression: Latte.Expression): StateOf[LLVM.Expression] = {
      expression match {

        case Latte.Null(nullType) => state(LLVM.Value("null", PointerType(nullType)))
        case Latte.Void => for (r <- getRegister(IntType)) yield r
        case Latte.ConstValue(value) if expression.getType == StringType => {
          val string = value
          for {
            register <- createStringConst(string.asInstanceOf[String])
          } yield register
        }

        case Latte.ConstValue(value) if expression.getType == BoolType => for {
          typeT <- transformType(expression.getType)
        } yield LLVM.Value(if (value.asInstanceOf[Boolean]) "1" else "0", typeT)

        case Latte.ConstValue(value) => for {
          typeT <- transformType(expression.getType)
        } yield LLVM.Value(value.toString, typeT)

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

            identifier <- (name match {
              case "printInt" => state(LLVM.FunctionId(name, VoidType))
              case "printString" => state(LLVM.FunctionId(name, VoidType))
              case definedName if functions contains definedName => for {
                funcType <- transformType(functions(definedName).returnType)
              } yield LLVM.FunctionId(definedName, funcType)
            }): StateOf[LLVM.FunctionId]

            returnRegister <- getRegister(identifier.returnType)
            argsRegisters <- arguments.toList.traverseS(e => compileExpression(e))
            _ <- putLine(LLVM.AssignFuncall(returnRegister, returnRegister.typeId,
              LLVM.call(returnRegister.typeId, s"@$name", argsRegisters)))
          } yield returnRegister: LLVM.Expression

        case Latte.FunctionCall(Latte.VTableLookup(exprU, offset, funcType), argumentsU) => for {
          expr <- compileExpression(exprU)
          exprType = expr.typeId.deref.asInstanceOf[ClassType]
          arguments <- mapM(argumentsU.toList, compileExpression)

          vTablePP <- getRegister(PointerType(PointerType(exprType.vtable)))
          vTableP <- getRegister(PointerType(exprType.vtable))
          functionPP <- getRegister(PointerType(PointerType(funcType)))
          functionP <- getRegister(PointerType(funcType))

          _ <- putLine(LLVM.Assign(vTablePP,
            LLVM.getElementPtr(expr.typeId.deref, expr.asInstanceOf[RegisterT], List(0, 0))))
          _ <- putLine(LLVM.Assign(vTableP, LLVM.load(vTablePP)))

          _ <- putLine(LLVM.Assign(functionPP,
            LLVM.getElementPtr(exprType.vtable, vTableP, List(0, offset))))
          _ <- putLine(LLVM.Assign(functionP, LLVM.load(functionPP)))

          returnRegister <- getRegister(funcType.returnType)

          _ <- putLine(LLVM.AssignFuncall(returnRegister, returnRegister.typeId,
            LLVM.call(funcType.returnType, functionP.name, expr :: arguments)))
        } yield returnRegister : LLVM.Expression

        case Latte.ArrayCreation(elementType, sizeExpr) => for {
          arrayType <- transformType(elementType)
          returnRegister <- getRegister(PointerType(arrayType))
          size <- compileExpression(sizeExpr)
          _ <- putLine(LLVM.Assign(returnRegister, LLVM.alloca(arrayType, Some(size))))
        } yield returnRegister

        case Latte.InstanceCreation(classType: ClassType) => for {
          instanceType <- transformType(classType)
          rawMemory <- getRegister(PointerType(CharType))
          returnRegister <- getRegister(PointerType(instanceType))
          placeholder <- getRegister(VoidType)

          classSize <- gets[CompilationState, Int](_.typeInformation.memsize(classType))

          _ <- putLine(LLVM.Literal(s"${rawMemory.name} = call i8* @malloc(i32 $classSize)"))
          _ <- putLine(LLVM.Literal(s"${returnRegister.name} = bitcast i8* ${rawMemory.name} to ${classType.ptr.llvmRepr}"))
          _ <- putLine(LLVM.AssignFuncall(placeholder, VoidType,
            LLVM.call(VoidType, s"@${classType.constructor}", List(returnRegister))))
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
        case Latte.Declaration(identifier, typeId) => for {
          transType <- transformType(typeId)
          _ <- allocate(identifier, transType)
        } yield Unit

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
        idType <- transformType(t)
        _ <- allocate(identifier, idType)
        argRegister <- getLocation(identifier)
        _ <- putLine(LLVM.Literal(s"store ${argRegister.typeId.deref.llvmRepr} %$identifier, ${argRegister.typeId.llvmRepr} ${argRegister.name}"))
        _ <- putSignature(rest)
      } yield Unit
    }

  def calcEltPtr(eltType: Type, sourceType: Type,
                 register: LLVM.RegisterT, indices: List[Int] = List(0, 0)): StateOf[LLVM.RegisterT] = for {
    result <- getRegister(eltType)
    _ <- putLine(
      LLVM.Assign(
        result,
        LLVM.getElementPtr(sourceType, register, List(0, 0))))
  } yield result

  def initializeFields(classType: Type.ClassType, thisPtr: LLVM.RegisterT): StateOf[Unit] = for {
    fields <- gets[CompilationState, Seq[Type]](_.typeInformation.field(classType).types)

    _ <- fields.zipWithIndex.toList.traverseS { case ((typeId, offset: Int)) => {
      val value = typeId match {
        case Type.IntType =>                         Latte.ConstValue(0)
        case Type.StringType =>                      Latte.ConstValue("")
        case c: Type.ClassType =>                    Latte.Null(c)
        case Type.PointerType(c: Type.ClassType) =>  Latte.Null(c)
      }

      for {
        location <- calculateFieldAddress(thisPtr, offset)
        _ <- calculateExpressionIntoRegister(location, value)
      } yield Unit
    }}
  } yield Unit

  def createConstructor(signature: Latte.FunctionSignature,
                        types: List[(String, Type.FunctionType)]): StateOf[LLVM.Block] = for {

    constructor <- blockFromCode(signature)
    classType = signature.arguments.head._2.deref.asInstanceOf[ClassType]

    _ <- putSignature(signature.arguments)
    thisPtr <- compileExpression(Latte.Variable(signature.arguments.head._1)).map(_.asInstanceOf[RegisterT])
    _ <- initializeFields(classType, thisPtr)

    vTablePP <- calcEltPtr(classType.vtable.ptr.ptr, thisPtr.typeId.deref, thisPtr)

    _ <- putLine(LLVM.Literal(s"store ${PointerType(classType.vtable).llvmRepr} ${classType.vtableDefault}, ${vTablePP.typeId.llvmRepr} ${vTablePP.name}"))

    _ <- putLine(LLVM.Return(None))

    codeBlock <- gets[CompilationState, Vector[LLVM.CodeBlock]](_.code)
  } yield constructor(codeBlock)

  type BlockCons = Vector[LLVM.CodeBlock] => LLVM.Block

  def blockFromCode(signature: Latte.FunctionSignature): StateOf[BlockCons] = for {
    funcSignature <- transformType(signature.returnType)
    functionId = LLVM.FunctionId(signature.identifier, funcSignature)

    newSignature <- signature.arguments.traverseS {
      case (name: String, t: Type) => for { transT <- transformType(t) } yield LLVM.register(name, transT)
    }

  } yield (b: Vector[LLVM.CodeBlock]) => LLVM.Block(functionId, newSignature, b)

  type GlobalDefinitions = String

  /**
    * Compile function
    * @return global static definitions needed by the function and its code
    */
  def compileFunction(compilationState: CompilationState): Latte.Func => (GlobalDefinitions, LLVM.Block) = {
    case Latte.Func(signature, Latte.BlockInstruction(codeBlock)) => {
      val codeCalculation: StateOf[(CompilationState, BlockCons)] = for {
        _ <- putSignature(signature.arguments)
        _ <- addManyLines(codeBlock)
        s <- get
        constructor <- blockFromCode(signature)
      } yield (s, constructor)

      val compiledFunction = codeCalculation(compilationState)._2

      (compiledFunction._1.globalCode, compiledFunction._2(compiledFunction._1.code))
    }

    case Latte.Func(signature, Latte.VtableFuncAssignment(funcs)) =>
      ("", createConstructor(signature, funcs)(compilationState)._2)
  }

  override def compile(code: Latte.Code): Either[List[CompileException], LLVM.Code] = {
    val compilationState = CompilationState(
      typeInformation = code.typeInformation,
      functionTypes = code.signatures)

    val (globalsFromFunctions, blocks) = (code.definitions map compileFunction(compilationState)).unzip
    val globals = globalsFromFunctions.mkString("\n") + "\n" + code.globalLLVM

    Right(LLVM.Code(globals, blocks.toList))
  }
}
