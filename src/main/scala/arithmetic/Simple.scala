package arithmetic


sealed trait StackOps
object StackOps {
    case class Const(const: Int) extends StackOps
    case object Add extends StackOps
    case object Mul extends StackOps
    case class Load(frame: Int) extends StackOps
    case class Store(frame: Int) extends StackOps
}

sealed trait StackExecutionError {
    
}

object StackExecution {
    type Stack = List[Int]
    type Executed[T] = Either[StackExecutionError, T]
    
    def execute(executedStack: Executed[Stack], op: StackOps): Executed[Stack] = for
    (stack <- executedStack)
    yield {
        import StackOps._
        (op, stack) match {
            case (Const(n), s) => n :: s
            case (Add, a :: b :: s) => (a+b) :: s
            case (Mul, a :: b :: s) => (a * b) :: s
        }
    }
    
    def executeAll(ops: List[StackOps]): Executed[Stack] = {
        val emptyStack: Executed[Stack] = Right(List())
        ops./:(emptyStack) (execute _)
    }
}

sealed trait HighLevelOp
object HighLevelOp {
    case class Const(x: Int) extends HighLevelOp
    case class Add(x: HighLevelOp, y: HighLevelOp) extends HighLevelOp
    case class Mul(x: HighLevelOp, y: HighLevelOp) extends HighLevelOp
 
    implicit def toConst(x: Int): HighLevelOp = Const(x)
}

object Compiler {
    type Program = List[StackOps]
    
    def compile(highLevel: HighLevelOp): Program = {
        highLevel match {
            case HighLevelOp.Const(x) => List(StackOps.Const(x))
            case HighLevelOp.Add(left, right) => {
                compile(left) ::: compile(right) ::: List(StackOps.Add)
            }
            case HighLevelOp.Mul(left, right) => {
                compile(left) ::: compile(right) ::: List(StackOps.Mul)
            }
        }
    }
}