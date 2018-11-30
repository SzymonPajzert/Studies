package compiler

import language.LLVM
import language.Type._
import parser.latte.Latte

import scalaz._
import scalaz.Scalaz._
import scalaz.State

object LatteToQuadCode extends Compiler[Latte.Code, LLVM.Code] {
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

  def getConst: StateOf[String] = for {
    counter <- increaseCounter
  } yield s"tmp$counter"

  def allocate(identifier: String, t: LLVMType): StateOf[Unit] = for {
    register <- getRegister(PointerType(t))
    _ <- putLine(LLVM.AssignCode(register, s"alloca $t"))
    state <- get[CompilationState]
    _ <- put (state.copy(currentSubstitutions = state.currentSubstitutions + (identifier -> register)))
  } yield Unit

  def getLocation(identifier: String): StateOf[LLVM.RegisterT] = for {
    location <- gets[CompilationState, LLVM.RegisterT] (state => state.currentSubstitutions(identifier))
  } yield location

  def getRegister(t : LLVMType = IntType): StateOf[LLVM.Register[t.type]] = for {
    tmpCounter <- increaseCounter
  } yield LLVM.Register(s"tmp$tmpCounter", t)


  /** Creates in global code const with that value and returns pointer to its value
    *
    * @param value
    * @return
    */
  def createStringConst(value: String): StateOf[LLVM.Register[PointerType]] = for {
    constName <- getConst
    arraySize = value.length + 1
    arrayType = s"[$arraySize x i8]"
    newLine = s"""@.$constName = private unnamed_addr constant $arrayType c"$value\\00""""
    _ <- modify[CompilationState] (state => state.copy(globalCode = state.globalCode + "\n" + newLine))
    register <- getRegister(PointerType(CharType))
    _ <- putLine(LLVM.AssignCode(register, s"getelementptr $arrayType, $arrayType* @.$constName, i64 0, i64 0"))
  } yield register


  def isPrimitiveName(str: String): Boolean = Set("int_add", "int_mul", "int_div", "int_sub") contains str

  def transformType(value: Type): LLVMType = value match {
    case IntType => IntType
    case VoidType => VoidType
    case CharType => CharType
    case BoolType => BoolType
    case a: PointerType => a
    case StringType => PointerType(CharType)
  }

  def compileExpression(expression: Latte.Expression): StateOf[LLVM.Expression] = {
    expression match {
      case Latte.ConstValue(value) if expression.getType == StringType => {
        val string = value
        for {
          register <- createStringConst(string.asInstanceOf[String])
        } yield register
      }
      case Latte.ConstValue(value) => for {
        register <- getRegister(transformType(expression.getType))
        _ <- putLine(LLVM.AssignRegister(register, value.asInstanceOf[Int]))
      } yield register
      case Latte.GetValue(identifier) => for {
        valueLocation <- getLocation(identifier)
        tmpRegister <- getRegister(transformType(valueLocation.typeId.deref))
        _ <- putLine(LLVM.AssignCode(tmpRegister, s"load ${valueLocation.typeId.deref}, ${valueLocation.typeId} ${valueLocation.name}"))
      } yield tmpRegister
      case Latte.FunctionCall(primitiveName, arguments) if isPrimitiveName(primitiveName) => {
        val Seq(left, right) = arguments

        val operation = primitiveName match {
          case "int_add" => LLVM.Add
          case "int_mul" => LLVM.Mul
          case "int_div" => LLVM.Div
          case "int_sub" => LLVM.Sub
        }

        for {
          leftValue <- compileExpression(left)
          rightValue <- compileExpression(right)
          returnRegister <- getRegister(leftValue.typeId)
          _ <- putLine(LLVM.AssignOp(returnRegister, operation, leftValue, rightValue))
        } yield returnRegister
      }

      case Latte.FunctionCall(primitiveName, arguments) if primitiveName == "gen_gt" => {
        val Seq(left, right) = arguments
        for {
          leftValue <- compileExpression(left)
          rightValue <- compileExpression(right)
          returnRegister <- getRegister(IntType)
          _ <- putLine(LLVM.AssignCode(returnRegister, s"icmp sgt ${leftValue.typeId} ${leftValue.name}, ${rightValue.name}"))
        } yield returnRegister
      }

      case Latte.FunctionCall(name, arguments) =>
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

  def addOneLine(instruction: Latte.Instruction): StateOf[Unit] = {
    instruction match {
      case Latte.Declaration(identifier, typeId) => allocate(identifier, transformType(typeId))
      case Latte.Assignment(identifier, value) => for {
        expression <- compileExpression(value)
        location <- getLocation(identifier)
        _ <- putLine(LLVM.Literal(s"store ${expression.typeId} ${expression.name}, ${location.typeId} ${location.name}"))

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

        _ <- putLine(LLVM.Jump(beginning))
        _ <- putJumpPoint(beginning)
        expressionRegister <- compileExpression(condition)
        _ <- putLine(LLVM.JumpIf(expressionRegister, ifTrue, ifFalse))
        _ <- putJumpPoint(ifTrue)
        _ <- addOneLine(instructions)
        _ <- putLine(LLVM.Jump(beginning))
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

    assert(code.lengthCompare(1) == 0)
    val mainFunction = code.head

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
