package compiler

import arithmetic.StackOps
import arithmetic.StackOps.PutPrint
import parser.{Instant, InstantProg}

object InstantToStackOp extends Compiler[InstantProg, StackOps.Code] {
  type Frames = Map[String, Int]
  type ProgramBuilder = (Frames, List[StackOps], Int)

  def getFrame(frames: Frames, identifier: String): (Frames, Int) = {
    val frameNumber = frames.getOrElse(identifier, frames.size)

    if (frameNumber == frames.size)
      (frames + (identifier -> frameNumber), frameNumber)
    else
      (frames, frameNumber)
  }

  def getOperator(operation: parser.Operation): StackOps = {
    StackOps.Operation(operation)
  }

  def getExpression(expr: parser.Expr, frames: Frames): ProgramBuilder = {
    import arithmetic.StackOps._
    import parser._

    expr match {
      case BinOp(leftExpr, op, rightExpr) => {
        val (framesLeft, left, leftSize) = getExpression(leftExpr, frames)
        val (framesRight, right, rightSize) = getExpression(rightExpr, framesLeft)

        val (mergedOps, totalSize) = if (op.isCommutative && rightSize > leftSize) {
          (right ::: left, math.max(rightSize, 1 + leftSize))
        } else {
          (left ::: right, math.max(leftSize, 1 + rightSize))
        }

        (framesRight, mergedOps ::: List(Operation(op)), totalSize + 1)
      }
      case Integer(value) => (frames, List(Const(value)), 1)
      case Value(identifier) => {
        val (newFrames, frameNumber) = getFrame(frames, identifier)
        (newFrames, List(Load(frameNumber)), 1)
      }
    }
  }

  def compileLine(code: Instant, frames: Frames): ProgramBuilder = {
    import parser._

    code match {
      case Print(expr) =>
        val expressionBuilder = getExpression(expr, frames)
        (expressionBuilder._1,
          PutPrint :: expressionBuilder._2 ::: List(StackOps.Print),
          math.max(1 + expressionBuilder._3, 3))
      case Assign(identifier, expr) =>
        val expressionBuilder = getExpression(expr, frames)
        val (newFrames, frameNumber) = getFrame(expressionBuilder._1, identifier)

        (newFrames,
          expressionBuilder._2 ::: List(StackOps.Store(frameNumber)),
          math.max(expressionBuilder._3, 2))
    }
  }

  override def compile(code: InstantProg): StackOps.Code = {
    val (allCompiledCode, allFrames, stackSize) = (code foldLeft (List[StackOps](), Map[String, Int](), 0)) (
      (accumulator, codeLine) => {
        val (compiledCode, frames, maxStackSize) = accumulator
        val newBuilder = compileLine(codeLine, frames)

        (compiledCode ::: newBuilder._2, newBuilder._1, math.max(maxStackSize, newBuilder._3))
      })

    StackOps.Code(allCompiledCode, stackSize, allFrames.size)
  }
}
