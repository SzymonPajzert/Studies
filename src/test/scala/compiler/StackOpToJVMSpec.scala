package compiler

import parser.instant
import arithmetic.StackOps
import backend.jvm.{JVMOp, JasminRunner}
import org.scalatest.{FlatSpec, Matchers}

class StackOpToJVMSpec extends FlatSpec with Matchers {
  behavior of "Stack to JVM compiler"

  def runStackCode(stackCode: StackOps.Code): List[String] = {
    val Right(compiledJVM) = StackOpToJVM.compile(stackCode)
    JasminRunner.runCode(JVMOp.createMain("Main", JVMOp.Code(4, 3, compiledJVM.code)))
  }

  it should "compile to easy program" in {
    val program = StackOps.Code(List(
      StackOps.PutPrint,
      StackOps.Const(1),
      StackOps.Const(2),
      StackOps.Operation(instant.Add),
      StackOps.Print
    ), 3, 0)

    assert(runStackCode(program) == List("3"))
  }

  it should "compile program with loading" in {
    val program = StackOps.Code(List(
      StackOps.PutPrint,
      StackOps.Const(1),
      StackOps.Const(2),
      StackOps.Store(0),
      StackOps.Store(1),
      StackOps.Load(1),
      StackOps.Load(0),

      StackOps.Operation(instant.Add),
      StackOps.Print
    ), 3, 2)

    assert(runStackCode(program) == List("3"))
  }

}
