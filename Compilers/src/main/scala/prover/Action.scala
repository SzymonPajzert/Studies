package prover


trait Environment {
  type Statement

  def axioms: List[Statement]
}

object Envs {
  def simple: Environment = new Environment {
    trait Statement
    case object True extends Statement


    override def axioms: List[Statement] = List(True)
  }

  def zlo: Environment = new Environment {
    override def axioms: List[Statement] = List(Subclass(P, NP))

    trait Statement
    case class Class(name: String)
    val P = Class("P")
    val NP = Class("NP")

    case class Subclass(a: Class, b: Class) extends Statement
  }
}

sealed trait Action {
  type T
}
case object Fail extends Action {
  type T = Nothing
}
case class Return[T0](value: T0) extends Action {
  type T = T0
}
case class Prove(environment: Environment)(statement: environment.Statement) extends Action {
  type T = Boolean
}
case class Parallel[T0](actions: Seq[Action]) extends Action {
  type T = T0
}

object Helper {
  def anySatisfies[A, T](element: Seq[A], actionMaker: (A => Action)): Action = {
    Parallel[T](element map actionMaker)
  }
}

trait Executor { self =>
  type E[T] = Either[Unit, T]

  def name: String

  def execute[T](action: Action): self.E[action.T]
}

object Executors {
  def get: List[Executor] = List(new CounterExecutor(1000))
}

class CounterExecutor(val counter: Int) extends Executor { self =>

  def name: String = s"CounterExecutor($counter)"

  def execute[T](action: Action): self.E[action.T] = {
    if(counter < 0) Left(Unit)

    action match {
      case Return(a) => Right(a.asInstanceOf[action.T])

      case p : Prove => {
        val axioms = p.environment.axioms

        val act = Helper.anySatisfies[p.environment.Statement, action.T](axioms, {
          case s      if s == p.=> Return(true)
          case _ => Fail
        } : PartialFunction[env.Statement, Action])

        self.decrease(1).execute(act.asInstanceOf[action.type])
      }

      case Parallel(actions) => {
        val parallelExecutor = self.divide(actions.length)
        val maybeActions = actions map (parallelExecutor.execute(_))
        (maybeActions find (_.isRight)).get.asInstanceOf[self.E[action.T]]
      }
    }
  }

  def divide(item: Int): self.type = new CounterExecutor(counter / item).asInstanceOf[self.type]
  def decrease(item: Int): self.type = new CounterExecutor(counter - item).asInstanceOf[self.type]
}

