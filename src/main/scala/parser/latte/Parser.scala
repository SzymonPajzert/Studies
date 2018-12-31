package parser.latte

import java.io.StringReader

import language.{HighLatte}
import language.Type._
import parser.{ParseError, Parser}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

object Transformations {
  import language.HighLatte._
  import latte.Absyn.{Type => AbsType, _}

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

      // TODO change to constructor call on object array
      override def visit(p: EArrayCons, arg: Any): Expression = ArrayCreation(convertType(p.type_), expression(p.expr_))

      override def visit(p: IVar, arg: Any): Expression = GetValue(p.ident_)

      override def visit(p: AVar, arg: Any): Expression = arrayAccess(p.arraye_)

      override def visit(p: FVar, arg: Any): Expression = fieldAccess(p.fielde_)

      override def visit(p: EClassCons, arg: Any): Expression = InstanceCreation(convertType(p.type_))

      override def visit(p: EMethod, arg: Any): Expression =
        FunctionCall(
          VTableLookup(expression(p.expr_), p.ident_),
          p.listexpr_.asScala map expression)


      override def visit(p: ECast, arg: Any): Expression = Cast(convertType(p.type_), expression(p.expr_))
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

  def fieldAccess(e: FieldE): FieldAccess = {
    val visitor = new FieldE.Visitor[FieldAccess, Any] {
      override def visit(p: FldAccess, arg: Any): FieldAccess =
        FieldAccess(expression(p.expr_), p.ident_)
    }

    e.accept(visitor, Unit)
  }

  def arrayAccess(e: ArrayE): ArrayAccess = {
    val visitor = new ArrayE.Visitor[ArrayAccess, Any] {
      override def visit(p: ArrAccess, arg: Any): ArrayAccess = {
        ArrayAccess(expression(p.expr_1), expression(p.expr_2))
      }
    }

    e.accept(visitor, Unit)
  }

  def instruction(instr: latte.Absyn.Stmt): List[HighLatte.Instruction] = {
    val visitor = new Stmt.Visitor[List[HighLatte.Instruction], Any] {
      type ReturnT = List[HighLatte.Instruction]

      override def visit(p: Empty, arg: Any): ReturnT = List()

      override def visit(p: BStmt, arg: Any): ReturnT = List(BlockInstruction(block(p.block_)))

      override def visit(p: Decl, arg: Any): ReturnT = (p.listitem_.asScala flatMap (pItem => {
        val item = extractItem(pItem)
        val declaration = Declaration(item._1, convertType(p.type_))
        val maybeAssignment = item._2.toList map (value => Assignment(item._1, value))
        declaration :: maybeAssignment
      })).toList

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
        HighLatte.While(expression(p.expr_), BlockInstruction(instruction(p.stmt_))))

      override def visit(p: SExp, arg: Any): ReturnT = List(
        DiscardValue(expression(p.expr_)))

      override def visit(p: AssArr, arg: Any): List[Instruction] = List(
        Assignment(
          arrayAccess(p.arraye_), expression(p.expr_))
      )

      override def visit(p: For, arg: Any): List[Instruction] = List(
        HighLatte.BlockInstruction(
          instruction(p.stmt_1) ++
          List(HighLatte.While(expression(p.expr_),
              BlockInstruction(
                instruction(p.stmt_3) ++ instruction(p.stmt_2))))))

      override def visit(p: AssFie, arg: Any): List[Instruction] = List(
        HighLatte.Assignment(fieldAccess(p.fielde_), expression(p.expr_))
      )

      override def visit(p: ForAbb, arg: Any): List[Instruction] = List(

      )
    }

    instr.accept(visitor, Unit)
  }

  def block(block: latte.Absyn.Block): HighLatte.Block = {
    block.accept((block, _: Any) => {
      (block.liststmt_.asScala flatMap instruction).toList
    }, Unit)
  }

  def funType(functionType: Fun): Type = {
    FunctionType(
      convertType(functionType.type_),
      functionType.listtype_.asScala map convertType)
  }

  def convertType(typeValue: latte.Absyn.Type): Type = {
    val visitor = new latte.Absyn.Type.Visitor[Type, Any] {
      override def visit(p: Int, arg: Any): Type = IntType
      override def visit(p: Str, arg: Any): Type = StringType
      override def visit(p: Bool, arg: Any): Type = BoolType
      override def visit(p: Void, arg: Any): Type = VoidType
      override def visit(p: Fun, arg: Any): Type = funType(p)
      override def visit(p: ArrayT, arg: Any): Type = ArrayType(convertType(p.type_), 0)
      override def visit(p: latte.Absyn.Class, arg: Any): Type = ClassType(p.ident_)
    }

    typeValue.accept(visitor, Unit)
  }

  def arguments(args: latte.Absyn.ListArg): Seq[(String, Type)] = {
    args.asScala map (arg => {
      val visitor = new Arg.Visitor[(String, Type), Any] {
        override def visit(p: ArgCons, arg: Any): (String, Type) = {
          (p.ident_, convertType(p.type_))
        }
      }
      arg.accept(visitor, Unit)
    })
  }

  def members(elts: ListClassElt) = elts.asScala.toList map (elt => {
    val visitor = new ClassElt.Visitor[HighLatte.ClassMember, Any] {
      override def visit(p: Field, arg: Any): ClassMember =
        HighLatte.Declaration(p.ident_, convertType(p.type_))

      override def visit(p: Method, arg: Any): ClassMember =
        HighLatte.Func(
          FunctionSignature(p.ident_, convertType(p.type_), arguments(p.listarg_).toList),
          block(p.block_))
    }

    elt.accept(visitor, Unit)
  })

  def topDefinition(definition: TopDef): TopDefinition = {
    val visitor = new TopDef.Visitor[TopDefinition, Any] {
      override def visit(p: FnDef, arg: Any): TopDefinition =
        HighLatte.Func(
          FunctionSignature(p.ident_, convertType(p.type_), arguments(p.listarg_).toList),
            block(p.block_))

      override def visit(p: ClDef, arg: Any): TopDefinition =
        HighLatte.Class(p.ident_, "Object", members(p.listclasselt_))

      override def visit(p: ClInh, arg: Any): TopDefinition =
        HighLatte.Class(p.ident_1, p.ident_2, members(p.listclasselt_))
    }

    definition.accept(visitor, Unit)
  }

  def program(latte: Program): HighLatte.Code = {
    val visitor = new Program.Visitor[HighLatte.Code, Any] {
      override def visit(p: ProgramCons, arg: Any): HighLatte.Code =
        p.listtopdef_.asScala map topDefinition
    }

    latte.accept(visitor, Unit)
  }
}

object LatteParser extends Parser[HighLatte.Code] {
  def parse(content: String): Either[List[ParseError], HighLatte.Code] = {
    val yylex = new latte.Yylex(new StringReader(content))
    val p = new latte.parser(yylex)

    try {
      Right(Transformations.program(p.pProgram))
    } catch {
      case e: Exception => Left(List(ParseError(yylex.line_num(), yylex.buff(), e.getMessage)))
    }
    // Left(List(ParseError(yylex.line_num(), yylex.buff(), e.getMessage)))
  }
}
