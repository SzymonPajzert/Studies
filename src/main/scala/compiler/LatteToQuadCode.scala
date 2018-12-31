package compiler

import language.Type._
import language.{LLVM, Latte}

import scalaz.Scalaz._
import scalaz.{State, _}

object LatteToQuadCode extends Compiler[Latte.Code, LLVM.Code] {
  // TODO remove globalCode section
  case class CompilationState(globalCode: String = "",
                              tmpCounter: Int = 0,
                              currentSubstitutions: Registers = Map(),
                              code: Vector[LLVM.CodeBlock] = Vector())

  type Registers = Map[String, LLVM.RegisterT]
  type StateOf[T] = State[CompilationState, T]
  type StateC = StateOf[List[LLVM.Instruction]]

  def increaseCounter: StateOf[Int] = for {
    state <- get[CompilationState]
    tmpCounter = state.tmpCounter
    _ <- put (state.copy(tmpCounter = tmpCounter + 1))
  } yield tmpCounter

  def getConst(t : LLVMType = IntType): StateOf[LLVM.Register[t.type]] = for {
    counter <- increaseCounter
  } yield LLVM.constRegister(s"tmp$counter", t)

  def allocate(identifier: String, t: LLVMType): StateOf[Unit] = for {
    register <- getRegister(PointerType(t))
    _ <- putLine(LLVM.Assign(register, LLVM.alloca(t)))
    state <- get[CompilationState]
    _ <- put (state.copy(currentSubstitutions = state.currentSubstitutions + (identifier -> register)))
  } yield Unit

  def getLocation(identifier: String): StateOf[LLVM.RegisterT] = for {
    location <- gets[CompilationState, LLVM.RegisterT] (_.currentSubstitutions(identifier))
  } yield location

  def getRegister(t : LLVMType = IntType): StateOf[LLVM.Register[t.type]] = for {
    tmpCounter <- increaseCounter
  } yield LLVM.register(s"tmp$tmpCounter", t)


  /** Creates in global code const with that value and returns pointer to its value
    *
    * @param value
    * @return
    */
  def createStringConst(value: String): StateOf[LLVM.Register[PointerType]] = for {
    constName <- getConst(PointerType(CharType))
    arrayType = ArrayType(CharType, value.length + 1)
    newLine = s"""${constName.name} = private unnamed_addr constant $arrayType c"$value\\00""""
    _ <- modify[CompilationState] (state => state.copy(globalCode = state.globalCode + "\n" + newLine))
    register <- getRegister(PointerType(CharType))
    _ <- putLine(LLVM.Assign(register, LLVM.getElementPtr(arrayType, constName)))
  } yield register

  def transformType(value: Type): LLVMType = value match {
    case IntType => IntType
    case VoidType => VoidType
    case CharType => CharType
    case BoolType => BoolType
    case a: PointerType => a
    case StringType => PointerType(CharType)
    case ArrayType(t, _) => PointerType(transformType(t))
  }

  def isPrimitiveName(location: Latte.FunLocation): Boolean = location match {
    case Latte.FunName(name) => Set("int_add", "int_mul", "int_div", "int_sub") contains name
    case Latte.VTableLookup(_, _) => false
  }

  def isIcmp(location: Latte.FunLocation): Option[Boolean] =
    location match {
      case Latte.FunName(primitiveName) => primitiveName match {
        case "gen_gt" => Some(false)
        case "gen_lt" => Some(true)
        case _ => None
      }
      case Latte.VTableLookup(_, _) => None
    }

  /**
    * Load value stored at the register
    * @param valueLocation Register of type (T*)
    * @return Register of type (T) with value loaded
    */
  def loadRegister(valueLocation: LLVM.RegisterT): StateOf[LLVM.RegisterT] = for {
    tmpRegister <- getRegister(transformType(valueLocation.typeId.deref))
    _ <- putLine(LLVM.Assign(tmpRegister, LLVM.load(valueLocation)))
  } yield tmpRegister

  /**
    * Given array access, calculate address where it is stored
    * @param arrayAccess Expression representing array access
    * @return Register of type (T*)
    */
  def calculateArrayAddress(arrayAccess: Latte.ArrayAccess): StateOf[LLVM.RegisterT] = {
    arrayAccess match {
      case Latte.ArrayAccess(Latte.GetValue(array), element) =>
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

  def compileExpression(expression: Latte.Expression): StateOf[LLVM.Expression] = {
    expression match {
      case Latte.ConstValue(value) if expression.getType == StringType => {
        val string = value
        for {
          register <- createStringConst(string.asInstanceOf[String])
        } yield register
      }
      case Latte.ConstValue(value) => {
        val typeT = transformType(expression.getType)
        for {
          register <- getRegister(typeT)
          _ <- putLine(LLVM.Assign(register, LLVM.expression(LLVM.Value(value.toString, typeT))))
        } yield register
      }
      case arrayAccess @ Latte.ArrayAccess(Latte.GetValue(_), _) => for {
        valueLocation <- calculateArrayAddress(arrayAccess)
        value <- loadRegister(valueLocation)
      } yield value

      case Latte.GetValue(identifier) => for {
        valueLocation <- getLocation(identifier)
        returnRegister <- loadRegister(valueLocation)
      } yield returnRegister


      case Latte.FunctionCall(primitiveName, arguments) if isPrimitiveName(primitiveName) => {
        val Seq(left, right) = arguments

        val operation = primitiveName match {
          case Latte.FunName("int_add") => LLVM.Add
          case Latte.FunName("int_mul") => LLVM.Mul
          case Latte.FunName("int_div") => LLVM.Div
          case Latte.FunName("int_sub") => LLVM.Sub
        }

        for {
          leftValue <- compileExpression(left)
          rightValue <- compileExpression(right)
          returnRegister <- getRegister(leftValue.typeId)
          _ <- putLine(LLVM.AssignOp(returnRegister, operation, leftValue, rightValue))
        } yield returnRegister
      }

      case Latte.ArrayCreation(elementType, sizeExpr) => for {
        returnRegister <- getRegister(PointerType(transformType(elementType)))
        size <- compileExpression(sizeExpr)
        _ <- putLine(LLVM.Assign(returnRegister, LLVM.alloca(transformType(elementType), Some(size))))
      } yield returnRegister

      case Latte.FunctionCall(primitiveName, arguments) if isIcmp(primitiveName).isDefined => {
        val Seq(left, right) = arguments
        val shouldSwitch = isIcmp(primitiveName).get

        for {
          leftValue <- compileExpression(left)
          rightValue <- compileExpression(right)
          returnRegister <- getRegister(IntType)
          _ <- putLine(LLVM.Assign(returnRegister,
            if(shouldSwitch) LLVM.icmpSgt(rightValue, leftValue)
            else LLVM.icmpSgt(leftValue, rightValue)))
        } yield returnRegister
      }

      case Latte.FunctionCall(Latte.FunName(name), arguments) =>
        val identifier = name match {
          case "printInt" => LLVM.FunctionId(name, VoidType)
          case "printString" => LLVM.FunctionId(name, VoidType)
        }

        for {
          returnRegister <- getRegister(identifier.returnType)
          argsRegisters <- arguments.toList.traverseS(compileExpression)
          _ <- putLine(LLVM.AssignFuncall(returnRegister, identifier, argsRegisters))
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
    _ <- put (state.copy(code = state.code :+ jumpPoint))
  } yield Unit

  /**
    * Given register to store the value, calculate expression and store it there
    * @param location Location to store the value
    * @param value Expression to be calculated
    * @return State of changed source code
    */
  def calculateExpressionIntoRegister(location: LLVM.RegisterT, value: Latte.Expression): StateOf[Unit] = for {
    expression <- compileExpression(value)
    _ <- putLine(LLVM.Literal(s"store ${expression.typeId} ${expression.name}, ${location.typeId} ${location.name}"))
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
      case Latte.DiscardValue(value) => for {
        _ <- compileExpression(value)
      } yield Unit
      case Latte.Return(valueUncompiled) => for {
        value <- (valueUncompiled map compileExpression) getOrElse state(LLVM.Value("0", IntType).asInstanceOf[LLVM.Expression])
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

  def addReturn: StateOf[Unit] = for {
    s <- get[CompilationState]
    _ <- if (!s.code.last.isInstanceOf[LLVM.Return])
      putLine(LLVM.Return(LLVM.value(0)))
    else state[CompilationState, Unit](Unit)
  } yield Unit

  override def compile(code: Latte.Code): Either[List[CompileException], LLVM.Code] = {

    assert(code.definitions.lengthCompare(1) == 0)
    val mainFunction = code.definitions.head

    mainFunction match {
      case Latte.Func(signature, codeBlock) => {
        val codeCalculation: StateOf[CompilationState] = for {
          _ <- addManyLines(codeBlock)
          _ <- addReturn
          s <- get
        } yield s

        val compiledFunction = codeCalculation(CompilationState())._2

        Right(LLVM.Code(
          compiledFunction.globalCode,
          List(LLVM.Block(LLVM.FunctionId("main", IntType), compiledFunction.code))))
      }
    }
  }
}
