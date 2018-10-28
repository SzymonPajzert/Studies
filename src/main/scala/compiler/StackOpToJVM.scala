package compiler

import arithmetic.StackOps
import jvm.JVMOp
import jvm.JVMOp.Block

trait StackOpToJVM {
  type Program = List[StackOps]
  def compile(arithmetic: Program): (Int, JVMOp.Block)
}

class StackOpToJVMDefault extends StackOpToJVM {
  def compile(arithmetic: Program) = {
    val jvmOps: JVMOp.Block = arithmetic flatMap translateOperator
    (4, jvmOps ::: List(JVMOp.Return))
  }

  def translateOperator(operator: StackOps): Block = {
    import JVMOp._

    operator match {
      case StackOps.PutPrint => List(GetStatic("java/lang/System/out Ljava/io/PrintStream;"))
      case StackOps.Print => List(InvokeVirtual("java/io/PrintStream/println(I)V"))
      case StackOps.Add => List(JVMOp.add[Int])
      case StackOps.Const(value) => List(JVMOp.const[Int](value))
      case StackOps.Load(frame) => List(JVMOp.load[Int](frame))
      case StackOps.Store(frame) => List(JVMOp.store[Int](frame))
    }
  }
}

object StackOpToJVM {
  def get: StackOpToJVM = new StackOpToJVMDefault()
}
