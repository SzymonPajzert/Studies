package compiler

import backend.llvm.LLVM
import backend.llvm.LLVM.{Expression, Register}
import parser.{Instant, InstantProg}

object InstantToLlvm extends Compiler[InstantProg, LLVM.Code] {
  case class CompilationState(tmpCounter: Int,
                              currentSubstitutions: Map[String, Expression]) {
    def getTmp: (LLVM.Register, CompilationState) = {
      (LLVM.Register(s"tmp$tmpCounter", LLVM.IntType),
        this.copy(tmpCounter = tmpCounter + 1))
    }

    def identifyValue(identifier: String, value: Expression): CompilationState = {
      this.copy(currentSubstitutions = currentSubstitutions + (identifier -> value))
    }

    def createRegister(identifier: String): (Register, CompilationState) = {
      val (tempRegister, newState) = getTmp
      (tempRegister, newState identifyValue (identifier, tempRegister))
    }

    def removeIdentifier(identifier: String): CompilationState = {
      if (currentSubstitutions contains identifier)
        this.copy(currentSubstitutions = currentSubstitutions - identifier)
      else this
    }
  }
  case class OneLine(code: LLVM.Code, state: CompilationState)

  override def compile(code: InstantProg): LLVM.Code = {
    (code foldLeft(List[LLVM.Instruction](), CompilationState(0, Map[String, Expression]()))) {
      (accumulator, line) => {
        val (generatedCode, currentSubstitutions) = accumulator
        val oneLine = compileLine(line, currentSubstitutions)
        (generatedCode ::: oneLine.code, oneLine.state)
      }
    }._1
  }

  def putIntoRegister(state: CompilationState,
                      expr: parser.Expr,
                      register: LLVM.Register): (OneLine, LLVM.Expression) = {
    val line = compileLine(parser.Assign(register.value, expr), state)

    line.state.currentSubstitutions get register.value match {
      case Some(value) => (line, value)
      case None => (line, register)
    }
  }

  def convert(op: parser.Operation): LLVM.Operation = op match {
    case parser.Add => LLVM.Add
    case parser.Mul => LLVM.Mul
    case parser.Div => LLVM.Div
    case parser.Sub => LLVM.Sub
  }


  def compileLine(line: Instant, state: CompilationState): OneLine = {
    import parser._

    line match {
      case Assign(identifier, Integer(value)) =>
        OneLine(LLVM.empty, state.identifyValue(identifier, value))

      case Assign(identifier, Value(valueIdentifier)) =>
        state.currentSubstitutions get valueIdentifier match {
          case x @ Some(LLVM.Value(_, _)) => OneLine(
            LLVM.empty,
            state.identifyValue(identifier, x.get))
          case x @ Some(LLVM.Register(_, _)) => {
            val (target, newState) = state.createRegister(identifier)
            OneLine(List(LLVM.AssignRegister(target, x.get.asInstanceOf[LLVM.Register])), newState)
          }
          case None => {
            throw new Exception("failed")
          }
        }

      case Assign(identifier, BinOp(leftExpr, op, rightExpr)) => {
        val (leftRegister, state1) = state.getTmp
        val (rightRegister, state2) = state1.getTmp

        val (leftLine, leftValue) = putIntoRegister(state2, leftExpr, leftRegister)
        val (rightLine, rightValue) = putIntoRegister(leftLine.state, rightExpr, rightRegister)

        val (register, state3) = rightLine.state.createRegister(identifier)

        val assignment = List(LLVM.AssignOp(register, convert(op), leftValue, rightValue))

        OneLine(leftLine.code ::: rightLine.code ::: assignment, state3)
      }

      case Print(expression) => {
        val (tempRegister, state1) = state.getTmp
        val (line, value) = putIntoRegister(state1, expression, tempRegister)
        OneLine(
          line.code ::: List(LLVM.PrintInt(value)),
          line.state)
      }
    }
  }
}
