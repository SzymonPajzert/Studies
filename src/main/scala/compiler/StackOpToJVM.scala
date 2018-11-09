package compiler

import parser.instant
import arithmetic.StackOps
import backend.jvm
import backend.jvm.JVMOp
import backend.jvm.JVMOp.Block

object StackOpToJVM extends Compiler[StackOps.Code, JVMOp.Code] {
  def compile(arithmetic: StackOps.Code) = {
    val jvmOps: JVMOp.Block = arithmetic.code flatMap translateOperator
    JVMOp.Code(stackSize = arithmetic.stackSize, arithmetic.framesUsed + 1, jvmOps ::: List(JVMOp.Return))
  }

  def translateOperator(operator: StackOps): Block = {
    import JVMOp._

    operator match {
      case StackOps.PutPrint => List(GetStatic("java/lang/System/out Ljava/io/PrintStream;"))
      case StackOps.Print => List(
        InvokeVirtual("java/io/PrintStream/println(I)V")
      )
      case StackOps.Operation(op) => op match {
        case instant.Add => List(JVMOp.add[Int])
        case instant.Mul => List(JVMOp.mul[Int])
        case instant.Sub => List(JVMOp.sub[Int])
        case instant.Div => List(JVMOp.div[Int])
      }
      case StackOps.Const(value) => List(JVMOp.const[Int](value))
      case StackOps.Load(frame) => List(JVMOp.load[Int](frame))
      case StackOps.Store(frame) => List(JVMOp.store[Int](frame))
    }
  }
}
