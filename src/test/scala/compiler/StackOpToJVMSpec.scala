package compiler

import arithmetic.StackOps
import jvm.{JVMOp, JasminRunner}
import org.scalatest.{FlatSpec, Matchers}

class StackOpToJVMSpec extends FlatSpec with Matchers {
  behavior of "Stack to JVM compiler"

  def runStackCode(stackCode: List[StackOps]): String = {
    val compiler: StackOpToJVM = StackOpToJVM.get
    val (stackDepth, assembler) = compiler.compile(stackCode)
    JasminRunner.run(JVMOp.createMain(stackDepth, assembler))
  }

  it should "compile to easy program" in {
    val program = List(
      StackOps.PutPrint,
      StackOps.Const(1),
      StackOps.Const(2),
      StackOps.Add,
      StackOps.Print,
    )

    assert(runStackCode(program) == "3")
  }

  it should "compile program with loading" in {
    val program = List(
      StackOps.PutPrint,
      StackOps.Const(1),
      StackOps.Const(2),
      StackOps.Store(0),

      // TODO(szymonpajzert): wtf

      StackOps.Load(0),
      StackOps.Add,
      StackOps.Print,
    )

    assert(runStackCode(program) == "3")
  }

}
