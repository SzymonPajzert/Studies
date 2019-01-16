package parser.instant

import java.io.StringReader

import compiler.ParseFailure
import instant.Absyn._
import parser.Parser
import instant.{Yylex, parser}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

sealed trait Instant { def toSource: String }
case class Assign(identifier: String, expr: Expr) extends Instant {
  override def toSource: String = s"$identifier = ${expr.toSource};"
}

case class Print(expr: Expr) extends Instant {
  override def toSource: String = s"printInt(${expr.toSource});"
}

sealed trait Operation { def isCommutative: Boolean }
case object Add extends Operation { val isCommutative = true }
case object Mul extends Operation { val isCommutative = true }
case object Div extends Operation { val isCommutative = false }
case object Sub extends Operation { val isCommutative = false }

sealed trait Expr {
  def toSource: String
}
case class BinOp(left: Expr, op: Operation, right: Expr) extends Expr {
  override def toSource: String = {
    val opStr = op match {
      case Add => "+"
      case Mul => "*"
      case Div => "/"
      case Sub => "-"
    }

    s"(${left.toSource}) $opStr (${right.toSource})"
  }
}

case class Integer(value: Int) extends Expr {
  override def toSource: String = value.toString
}

case class Value(identifier: String) extends Expr {
  override def toSource: String = identifier
}

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


object InstantParser extends Parser[InstantProg] {
  def getYylex(input: String): Yylex = {
    new Yylex(new StringReader(input))
  }

  def parse(input: String): Either[ParseFailure, InstantProg] = {
    val yylex = getYylex(input)
    val p = new parser(yylex)

    try
    {
      Right(Transformations.program(p.pProgram))
    }
    catch {
      case e: Throwable =>  Left(ParseFailure(yylex.line_num(), yylex.buff(), e.getMessage))
    }
  }
}