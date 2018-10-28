package arithmetic

import scala.util.{Either, Left, Right}
import org.scalatest._

class SimpleArithmeticSpec extends FlatSpec with Matchers {
    behavior of "Arithmetic compilator"
    
    it should "put values on stack" in {
        import StackOps._
        
        val executionResult = StackExecution.executeAll(List(Const(1)))
        executionResult shouldEqual Right(List(1))
    }
    
    it should "add values on stack" in {
        import StackOps._
        
        val prog = List(Const(1), Const(2), Add)
        val executionResult = StackExecution.executeAll(prog)
        executionResult shouldEqual Right(List(3))
    }
}

class CompilerSpec extends FlatSpec with Matchers {
    behavior of "Execution of compiled code"
    
    it should "return constants" in {
        import HighLevelOp._
        import Compiler.Program
        
        val op = Const(10)
        val compiled: Program = Compiler.compile(op)
        val executionResult = StackExecution.executeAll(compiled)
        
        executionResult shouldEqual Right(List(10))
    }
    
    it should "add values" in {
        import HighLevelOp._
        import Compiler.Program
        
        val op = Add(1, 2)
        val compiled: Program = Compiler.compile(op)
        val executionResult = StackExecution.executeAll(compiled)
        
        executionResult shouldEqual Right(List(3))
    }
    
    it should "add nested values" in {
        import HighLevelOp._
        import Compiler.Program
        
        val op = Add(1, Add(2, 3))
        val compiled: Program = Compiler.compile(op)
        val executionResult = StackExecution.executeAll(compiled)
        
        executionResult shouldEqual Right(List(6))
    }
    
    it should "multiply values" in {
        import HighLevelOp._
        import Compiler.Program
        
        val op = Mul(2, 3)
        val compiled: Program = Compiler.compile(op)
        val executionResult = StackExecution.executeAll(compiled)
        
        executionResult shouldEqual Right(List(6))
    }
}
 