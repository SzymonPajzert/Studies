// reviewed: 2018.12.28

package compiler

import language.Latte

import scalaz.Scalaz._
import scalaz._

/**
  * Performs following checks:
  *   - static binding of variables
  *   - renaming hiding variables
  */
object LatteStaticAnalysis extends Compiler[Latte.Code, Latte.Code] {
  case class VariableState(counter: Int, fromCurrentBlock: Boolean)
  type Variables = Map[String, VariableState]
  type S[A] = StateT[Id, Variables, A]
  type Compiler[E] = State[Variables, List[CompileException] \/ E]

  def mapM[A, B, C](a: List[A], f: A => List[B] \/ C): List[B] \/ List[C] = {
    (a map f).foldLeft(List().right : List[B] \/ List[C]) {
      case (acc, elt) => (acc, elt) match {
        case (-\/(x), -\/(y)) => -\/(x ::: y)
        case (-\/(x), \/-(_)) => -\/(x)
        case (\/-(_), -\/(y)) => -\/(y)
        case (\/-(x), \/-(y)) => \/-(x ::: List(y))
      }
    }
  }

  def markAsOld(variables: Variables): Variables = {
    variables.map(pair => (pair._1, pair._2.copy(fromCurrentBlock = false)))
  }

  def getVariable(identifier: String): Compiler[String] = for {
    variables <- get[Variables]
  } yield variables get identifier match {
    case Some(state) => \/-(identifier + state.counter.toString)
    case None => -\/(List(ErrorString(s"Undefined variable $identifier")))
  }

  def compileExpr(expression: Latte.Expression): Compiler[Latte.Expression] = {
    expression match {
      case Latte.GetValue(identifier) => for {
        nameC <- getVariable(identifier)
      } yield nameC map (n => Latte.GetValue(n))
      case Latte.FunctionCall(functionName, arguments) => for {
        currentState <- get[Variables]
        argumentsC = mapM(arguments.toList, runOn(compileExpr, currentState))
      } yield argumentsC map (a => Latte.FunctionCall(functionName, a): Latte.Expression)
      case _ => state(\/-(expression))
    }
  }

  def newVariable(variableName: String): Compiler[String] = for {
    vars <- get[Variables]

    counterValue = vars get variableName match {
      case None => Some(0)
      case Some(state) if !state.fromCurrentBlock => Some(state.counter + 1)
      case _ => None
    }

    _ <- counterValue match {
      case None => modify[Variables](a => a)
      case Some(value) =>
        modify[Variables](variables => variables + (variableName -> VariableState(value, true)))
    }
  } yield counterValue match {
    case None => -\/[List[CompileException]](List(ErrorString(s"Redefinion of variable $variableName")))
    case Some(value) => \/-[String](variableName + value.toString)
  }

  def modifyHead(instruction: Latte.Instruction): Compiler[Latte.Instruction] = instruction match {
    case Latte.Declaration(name, typeDecl) => for {
      variableNameC <- newVariable(name)
    } yield variableNameC map (v => Latte.Declaration(v, typeDecl))
    case Latte.Assignment(name, expression) => for {
      nameC <- getVariable(name)
      expressionC <- compileExpr(expression)
      // TODO check if those nests work
    } yield expressionC flatMap (e => nameC map (n => Latte.Assignment(n, e): Latte.Instruction))
    case Latte.BlockInstruction(instructions) => for {
      copyState <- get[Variables]
      transformedC = mapWithEnvironment(instructions)(markAsOld(copyState))._2
    } yield transformedC map (t => Latte.BlockInstruction(t): Latte.Instruction)
    case Latte.DiscardValue(expression) => for {
      expressionC <- compileExpr(expression)
    } yield expressionC map (e => Latte.DiscardValue(e))
    case Latte.Return(maybeExpression) => maybeExpression match {
      case Some(expr) => for {
        exprC <- compileExpr(expr)
      } yield exprC map (e => Latte.Return(Some(e)))
      case None => state(\/-(Latte.Return(None)))
    }
    case Latte.While(condition, instructions) => for {
      conditionC <- compileExpr(condition)
      instructionsC <- modifyHead(instructions)
    } yield conditionC flatMap  (c => instructionsC map (i => Latte.While(c, i)))
  }

  def mapWithEnvironment(code: List[Latte.Instruction]): Compiler[List[Latte.Instruction]] = code match {
    case Nil => state(\/-(Nil))
    case (h :: t) => for {
      he <- modifyHead(h)
      tailTransformed <- mapWithEnvironment(t)
    } yield for {
      hee <- he
      a <- tailTransformed
    } yield hee :: a
  }

  def emptyEnvironment: Variables = Map()

  def compileFunc(func: Latte.TopDefinition): Compiler[Latte.TopDefinition] = {
    func match {
      case Latte.Func(signature, code) => for {
        codeParsed <- mapWithEnvironment(code)
      } yield for {
        withoutError <- codeParsed
      } yield Latte.Func(signature, withoutError)
    }
  }

  private def runOn[A](f: A => Compiler[A], env: Variables): (A => List[CompileException] \/ A) = {
    (a: A) => f(a)(env)._2
  }

  override def compile(code: Latte.Code): Either[List[CompileException], Latte.Code] =
    mapM(code.toList, runOn(compileFunc, emptyEnvironment)).toEither
}
