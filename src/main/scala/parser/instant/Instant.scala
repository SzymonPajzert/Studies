package parser.instant

import java.io.{InputStreamReader, StringReader}

import collection.JavaConverters._
import instant.Absyn._
import instant.{Yylex, parser}

import scala.language.implicitConversions

sealed trait Instant
case class Assign(identifier: String, expr: Expr) extends Instant
case class Print(expr: Expr) extends Instant

sealed trait Operation { def isCommutative: Boolean }
case object Add extends Operation { val isCommutative = true }
case object Mul extends Operation { val isCommutative = true }
case object Div extends Operation { val isCommutative = false }
case object Sub extends Operation { val isCommutative = false }

sealed trait Expr
case class BinOp(left: Expr, op: Operation, right: Expr) extends Expr
case class Integer(value: Int) extends Expr
case class Value(identifier: String) extends Expr

object Transformations {
  def expression(exp: Exp): Expr = {
    implicit def to_exp(exp: Exp): Expr = expression(exp)

    val visitor = new Exp.Visitor[Expr, Any] {
      override def visit(p: ExpAdd, arg: Any): Expr = BinOp(p.exp_1, Add, p.exp_2)

      override def visit(p: ExpSub, arg: Any): Expr = BinOp(p.exp_1, Sub, p.exp_2)

      override def visit(p: ExpMul, arg: Any): Expr = BinOp(p.exp_1, Mul, p.exp_2)

      override def visit(p: ExpDiv, arg: Any): Expr = BinOp(p.exp_1, Div, p.exp_2)

      override def visit(p: ExpLit, arg: Any): Expr = Integer(p.integer_.intValue())

      override def visit(p: ExpVar, arg: Any): Expr = Value(p.ident_)
    }

    exp.accept(visitor, Unit)
  }

  def statement(statement: Stmt): Instant = {
    val visitor = new Stmt.Visitor[Instant, Any] {
      override def visit(p: SAss, arg: Any): Instant = Assign(p.ident_, expression(p.exp_))

      override def visit(p: SExp, arg: Any): Instant = Print(expression(p.exp_))
    }

    statement.accept(visitor, Unit)
  }

  def program(parse_tree: Program): InstantProg = {
    val visitor = new Program.Visitor[InstantProg, Any] {
      override def visit(p: Prog, arg: Any): InstantProg = {
        (p.liststmt_.asScala map statement).toList
      }
    }

    parse_tree.accept(visitor, Unit)
  }
}


object Parser {
  case class ParseError(lineNumber: Int, near: String, errorMsg: String)

  def getYylex(input: String): Yylex = {
    new Yylex(new StringReader(input))
  }

  type ParseResult = Either[InstantProg, ParseError]

  def parse(input: String): ParseResult = {
    val yylex = getYylex(input)
    val p = new parser(yylex)

    try
    {
      Left(Transformations.program(p.pProgram))
    }
    catch {
      case e: Throwable =>  Right(ParseError(yylex.line_num(), yylex.buff(), e.getMessage))
    }
  }
}