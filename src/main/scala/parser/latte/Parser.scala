package parser.latte

import java.io.StringReader

import collection.JavaConverters._
import scala.language.implicitConversions

trait Type
case object IntType extends Type
case object StringType extends Type
case object BoolType extends Type
case object VoidType extends Type
case class FunctionType(returnType: Type, argsType: Seq[Type]) extends Type

trait Expression
case class FunctionCall(name: String, arguments: Seq[Expression]) extends Expression
case class GetValue(identifier: String) extends Expression
case class ConstValue[+T](value: T) extends Expression

trait TopDefinition
case class Function(signature: FunctionSignature, code: Block) extends TopDefinition
case class FunctionSignature(identifier: String, returnType: Type, arguments: List[(String, Type)])

trait Instruction
case class Declaration(identifier: String, valueExpr: Option[Expression], typeValue: Type) extends Instruction
case class Assignment(identifier: String, expr: Expression) extends Instruction
case class BlockInstruction(block: Block) extends Instruction
case class DiscardValue(expression: Expression) extends Instruction
case class Return(value: Option[Expression]) extends Instruction
case class IfThen(condition: Expression, thenInst: Instruction, elseOpt: Option[Instruction] = None) extends Instruction
case class While(condition: Expression, instr: Instruction) extends Instruction

object Transformations {
  import latte.Absyn._

  def const(i: scala.Int): Expression = ConstValue[scala.Int](i)
  def const(b: Boolean): Expression = ConstValue[Boolean](b)
  def const(s: String): Expression = ConstValue[String](s)

  def getOperator(op: AddOp): String = {
    val visitor = new AddOp.Visitor[String, Any] {
      override def visit(p: Plus, arg: Any): String = "int_add"

      override def visit(p: Minus, arg: Any): String = "int_sub"
    }

    op.accept(visitor, Unit)
  }

  def getOperator(op: MulOp): String = {
    val visitor = new MulOp.Visitor[String, Any] {
      override def visit(p: Times, arg: Any): String = "int_mul"

      override def visit(p: Div, arg: Any): String = "int_div"

      override def visit(p: Mod, arg: Any): String = "int_mod"
    }

    op.accept(visitor, Unit)
  }

  def getOperator(op: RelOp): String = {
    val visitor = new RelOp.Visitor[String, Any] {
      override def visit(p: LTH, arg: Any): String = "gen_lt"

      override def visit(p: LE, arg: Any): String = "gen_le"

      override def visit(p: GTH, arg: Any): String = "gen_gt"

      override def visit(p: GE, arg: Any): String = "gen_ge"

      override def visit(p: EQU, arg: Any): String = "gen_eq"

      override def visit(p: NE, arg: Any): String = "gen_neq"
    }

    op.accept(visitor, Unit)
  }

  def expression(expr: Expr): Expression = {
    val visitor = new Expr.Visitor[Expression, Any] {
      override def visit(p: EVar, arg: Any): Expression = GetValue(p.ident_)

      override def visit(p: ELitInt, arg: Any): Expression = const(Integer.valueOf(p.integer_))

      override def visit(p: ELitTrue, arg: Any): Expression = const(true)

      override def visit(p: ELitFalse, arg: Any): Expression = const(false)

      override def visit(p: EApp, arg: Any): Expression = FunctionCall(
        p.ident_,
        p.listexpr_.asScala map expression)

      override def visit(p: EString, arg: Any): Expression = const(p.string_)

      override def visit(p: Neg, arg: Any): Expression = FunctionCall(
        "int_sub",
        List(const(0), expression(p.expr_))
      )

      override def visit(p: Not, arg: Any): Expression = FunctionCall(
        "bool_not", List(expression(p.expr_))
      )

      override def visit(p: EMul, arg: Any): Expression = FunctionCall(
        getOperator(p.mulop_), List(expression(p.expr_1), expression(p.expr_2)))

      override def visit(p: EAdd, arg: Any): Expression = FunctionCall(
        getOperator(p.addop_), List(expression(p.expr_1), expression(p.expr_2)))

      override def visit(p: ERel, arg: Any): Expression = FunctionCall(
        getOperator(p.relop_), List(expression(p.expr_1), expression(p.expr_2)))

      override def visit(p: EAnd, arg: Any): Expression = FunctionCall(
        "bool_and", List(expression(p.expr_1), expression(p.expr_2)))

      override def visit(p: EOr, arg: Any): Expression = FunctionCall(
        "bool_or", List(expression(p.expr_1), expression(p.expr_2)))
    }

    expr.accept(visitor, Unit)
  }

  def extractItem(item: Item): (String, Option[Expression]) = {
    val visitor = new Item.Visitor[(String, Option[Expression]), Any] {
      override def visit(p: NoInit, arg: Any): (String, Option[Expression]) = (p.ident_, None)

      override def visit(p: Init, arg: Any): (String, Option[Expression]) = (p.ident_, Some(expression(p.expr_)))
    }

    item.accept(visitor, Unit)
  }

  def instruction(instr: latte.Absyn.Stmt): Seq[parser.latte.Instruction] = {
    val visitor = new Stmt.Visitor[Seq[parser.latte.Instruction], Any] {
      type ReturnT = Seq[parser.latte.Instruction]

      override def visit(p: Empty, arg: Any): ReturnT = List()

      override def visit(p: BStmt, arg: Any): ReturnT = List(BlockInstruction(block(p.block_)))

      override def visit(p: Decl, arg: Any): ReturnT = p.listitem_.asScala map (pItem => {
        val item = extractItem(pItem)
        Declaration(item._1, item._2, convertType(p.type_))
      })

      override def visit(p: Ass, arg: Any): ReturnT = List(Assignment(p.ident_, expression(p.expr_)))

      override def visit(p: Incr, arg: Any): ReturnT = List(
        Assignment(p.ident_, FunctionCall("int_add", List(GetValue(p.ident_), const(1)))))

      override def visit(p: Decr, arg: Any): ReturnT = List(
        Assignment(p.ident_, FunctionCall("int_sub", List(GetValue(p.ident_), const(1)))))

      override def visit(p: Ret, arg: Any): ReturnT = List(Return(Some(expression(p.expr_))))

      override def visit(p: VRet, arg: Any): ReturnT = List(Return(None))

      override def visit(p: Cond, arg: Any): ReturnT = List(
        IfThen(expression(p.expr_), BlockInstruction(instruction(p.stmt_)))
      )

      override def visit(p: CondElse, arg: Any): ReturnT = List(
        IfThen(
          expression(p.expr_),
          BlockInstruction(instruction(p.stmt_1)),
          Some(BlockInstruction(instruction(p.stmt_1))))
      )

      override def visit(p: latte.Absyn.While, arg: Any): ReturnT = List(
        parser.latte.While(expression(p.expr_), BlockInstruction(instruction(p.stmt_))))

      override def visit(p: SExp, arg: Any): ReturnT = List(
        DiscardValue(expression(p.expr_)))
    }

    instr.accept(visitor, Unit)
  }

  def block(block: latte.Absyn.Block): parser.latte.Block = {
    block.accept((block, _: Any) => {
      block.liststmt_.asScala flatMap instruction
    }, Unit)
  }

  def funType(functionType: Fun): parser.latte.Type = {
    FunctionType(
      convertType(functionType.type_),
      functionType.listtype_.asScala map convertType)
  }

  def convertType(typeValue: latte.Absyn.Type): parser.latte.Type = {
    val visitor = new latte.Absyn.Type.Visitor[parser.latte.Type, Any] {
      override def visit(p: Int, arg: Any): parser.latte.Type = IntType
      override def visit(p: Str, arg: Any): parser.latte.Type = StringType
      override def visit(p: Bool, arg: Any): parser.latte.Type = BoolType
      override def visit(p: Void, arg: Any): parser.latte.Type = VoidType
      override def visit(p: Fun, arg: Any): parser.latte.Type = funType(p)
    }

    typeValue.accept(visitor, Unit)
  }

  def arguments(args: latte.Absyn.ListArg): Seq[(String, parser.latte.Type)] = {
    args.asScala map (arg => {
      val visitor = new Arg.Visitor[(String, parser.latte.Type), Any] {
        override def visit(p: ArgCons, arg: Any): (String, parser.latte.Type) = {
          (p.ident_, convertType(p.type_))
        }
      }
      arg.accept(visitor, Unit)
    })
  }

  def topDefinition(definition: TopDef): TopDefinition = {
    val visitor = new TopDef.Visitor[TopDefinition, Any] {
      override def visit(p: FnDef, arg: Any): TopDefinition =
        parser.latte.Function(
          FunctionSignature(p.ident_, convertType(p.type_), arguments(p.listarg_).toList),
            block(p.block_))
    }

    definition.accept(visitor, Unit)
  }

  def program(latte: Program): Latte = {
    val visitor = new Program.Visitor[Latte, Any] {
      override def visit(p: ProgramCons, arg: Any): Latte = p.listtopdef_.asScala map topDefinition
    }

    latte.accept(visitor, Unit)
  }
}

object Parser {
  case class ParseError(lineNumber: Int, near: String, errorMsg: String)

  def parse(content: String): Either[Unit, ParseError] = {
    val yylex = new latte.Yylex(new StringReader(content))
    val p = new latte.parser(yylex)

    try
    {
      Left(Transformations.program(p.pProgram))
    }
    catch {
      case e: Throwable =>  Right(ParseError(yylex.line_num(), yylex.buff(), e.getMessage))
    }
  }
}
