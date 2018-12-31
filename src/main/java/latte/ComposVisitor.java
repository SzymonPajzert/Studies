package latte;
import latte.Absyn.*;
/** BNFC-Generated Composition Visitor
*/

public class ComposVisitor<A> implements
  latte.Absyn.Program.Visitor<latte.Absyn.Program,A>,
  latte.Absyn.TopDef.Visitor<latte.Absyn.TopDef,A>,
  latte.Absyn.ClassElt.Visitor<latte.Absyn.ClassElt,A>,
  latte.Absyn.Arg.Visitor<latte.Absyn.Arg,A>,
  latte.Absyn.Block.Visitor<latte.Absyn.Block,A>,
  latte.Absyn.Stmt.Visitor<latte.Absyn.Stmt,A>,
  latte.Absyn.Item.Visitor<latte.Absyn.Item,A>,
  latte.Absyn.Type.Visitor<latte.Absyn.Type,A>,
  latte.Absyn.ArrayE.Visitor<latte.Absyn.ArrayE,A>,
  latte.Absyn.FieldE.Visitor<latte.Absyn.FieldE,A>,
  latte.Absyn.Expr.Visitor<latte.Absyn.Expr,A>,
  latte.Absyn.AddOp.Visitor<latte.Absyn.AddOp,A>,
  latte.Absyn.MulOp.Visitor<latte.Absyn.MulOp,A>,
  latte.Absyn.RelOp.Visitor<latte.Absyn.RelOp,A>
{
/* Program */
    public Program visit(latte.Absyn.ProgramCons p, A arg)
    {
      ListTopDef listtopdef_ = new ListTopDef();
      for (TopDef x : p.listtopdef_)
      {
        listtopdef_.add(x.accept(this,arg));
      }
      return new latte.Absyn.ProgramCons(listtopdef_);
    }
/* TopDef */
    public TopDef visit(latte.Absyn.FnDef p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      String ident_ = p.ident_;
      ListArg listarg_ = new ListArg();
      for (Arg x : p.listarg_)
      {
        listarg_.add(x.accept(this,arg));
      }
      Block block_ = p.block_.accept(this, arg);
      return new latte.Absyn.FnDef(type_, ident_, listarg_, block_);
    }    public TopDef visit(latte.Absyn.ClInh p, A arg)
    {
      String ident_1 = p.ident_1;
      String ident_2 = p.ident_2;
      ListClassElt listclasselt_ = new ListClassElt();
      for (ClassElt x : p.listclasselt_)
      {
        listclasselt_.add(x.accept(this,arg));
      }
      return new latte.Absyn.ClInh(ident_1, ident_2, listclasselt_);
    }    public TopDef visit(latte.Absyn.ClDef p, A arg)
    {
      String ident_ = p.ident_;
      ListClassElt listclasselt_ = new ListClassElt();
      for (ClassElt x : p.listclasselt_)
      {
        listclasselt_.add(x.accept(this,arg));
      }
      return new latte.Absyn.ClDef(ident_, listclasselt_);
    }
/* ClassElt */
    public ClassElt visit(latte.Absyn.Field p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      String ident_ = p.ident_;
      return new latte.Absyn.Field(type_, ident_);
    }    public ClassElt visit(latte.Absyn.Method p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      String ident_ = p.ident_;
      ListArg listarg_ = new ListArg();
      for (Arg x : p.listarg_)
      {
        listarg_.add(x.accept(this,arg));
      }
      Block block_ = p.block_.accept(this, arg);
      return new latte.Absyn.Method(type_, ident_, listarg_, block_);
    }
/* Arg */
    public Arg visit(latte.Absyn.ArgCons p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      String ident_ = p.ident_;
      return new latte.Absyn.ArgCons(type_, ident_);
    }
/* Block */
    public Block visit(latte.Absyn.BlockCons p, A arg)
    {
      ListStmt liststmt_ = new ListStmt();
      for (Stmt x : p.liststmt_)
      {
        liststmt_.add(x.accept(this,arg));
      }
      return new latte.Absyn.BlockCons(liststmt_);
    }
/* Stmt */
    public Stmt visit(latte.Absyn.Empty p, A arg)
    {
      return new latte.Absyn.Empty();
    }    public Stmt visit(latte.Absyn.BStmt p, A arg)
    {
      Block block_ = p.block_.accept(this, arg);
      return new latte.Absyn.BStmt(block_);
    }    public Stmt visit(latte.Absyn.Decl p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      ListItem listitem_ = new ListItem();
      for (Item x : p.listitem_)
      {
        listitem_.add(x.accept(this,arg));
      }
      return new latte.Absyn.Decl(type_, listitem_);
    }    public Stmt visit(latte.Absyn.Ass p, A arg)
    {
      String ident_ = p.ident_;
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.Ass(ident_, expr_);
    }    public Stmt visit(latte.Absyn.AssArr p, A arg)
    {
      ArrayE arraye_ = p.arraye_.accept(this, arg);
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.AssArr(arraye_, expr_);
    }    public Stmt visit(latte.Absyn.AssFie p, A arg)
    {
      FieldE fielde_ = p.fielde_.accept(this, arg);
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.AssFie(fielde_, expr_);
    }    public Stmt visit(latte.Absyn.Incr p, A arg)
    {
      String ident_ = p.ident_;
      return new latte.Absyn.Incr(ident_);
    }    public Stmt visit(latte.Absyn.Decr p, A arg)
    {
      String ident_ = p.ident_;
      return new latte.Absyn.Decr(ident_);
    }    public Stmt visit(latte.Absyn.Ret p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.Ret(expr_);
    }    public Stmt visit(latte.Absyn.VRet p, A arg)
    {
      return new latte.Absyn.VRet();
    }    public Stmt visit(latte.Absyn.Cond p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      Stmt stmt_ = p.stmt_.accept(this, arg);
      return new latte.Absyn.Cond(expr_, stmt_);
    }    public Stmt visit(latte.Absyn.CondElse p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      Stmt stmt_1 = p.stmt_1.accept(this, arg);
      Stmt stmt_2 = p.stmt_2.accept(this, arg);
      return new latte.Absyn.CondElse(expr_, stmt_1, stmt_2);
    }    public Stmt visit(latte.Absyn.While p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      Stmt stmt_ = p.stmt_.accept(this, arg);
      return new latte.Absyn.While(expr_, stmt_);
    }    public Stmt visit(latte.Absyn.For p, A arg)
    {
      Stmt stmt_1 = p.stmt_1.accept(this, arg);
      Expr expr_ = p.expr_.accept(this, arg);
      Stmt stmt_2 = p.stmt_2.accept(this, arg);
      Stmt stmt_3 = p.stmt_3.accept(this, arg);
      return new latte.Absyn.For(stmt_1, expr_, stmt_2, stmt_3);
    }    public Stmt visit(latte.Absyn.ForAbb p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      String ident_ = p.ident_;
      Expr expr_ = p.expr_.accept(this, arg);
      Stmt stmt_ = p.stmt_.accept(this, arg);
      return new latte.Absyn.ForAbb(type_, ident_, expr_, stmt_);
    }    public Stmt visit(latte.Absyn.SExp p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.SExp(expr_);
    }
/* Item */
    public Item visit(latte.Absyn.NoInit p, A arg)
    {
      String ident_ = p.ident_;
      return new latte.Absyn.NoInit(ident_);
    }    public Item visit(latte.Absyn.Init p, A arg)
    {
      String ident_ = p.ident_;
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.Init(ident_, expr_);
    }
/* Type */
    public Type visit(latte.Absyn.Int p, A arg)
    {
      return new latte.Absyn.Int();
    }    public Type visit(latte.Absyn.Str p, A arg)
    {
      return new latte.Absyn.Str();
    }    public Type visit(latte.Absyn.Bool p, A arg)
    {
      return new latte.Absyn.Bool();
    }    public Type visit(latte.Absyn.Void p, A arg)
    {
      return new latte.Absyn.Void();
    }    public Type visit(latte.Absyn.Class p, A arg)
    {
      String ident_ = p.ident_;
      return new latte.Absyn.Class(ident_);
    }    public Type visit(latte.Absyn.Fun p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      ListType listtype_ = new ListType();
      for (Type x : p.listtype_)
      {
        listtype_.add(x.accept(this,arg));
      }
      return new latte.Absyn.Fun(type_, listtype_);
    }    public Type visit(latte.Absyn.ArrayT p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      return new latte.Absyn.ArrayT(type_);
    }
/* ArrayE */
    public ArrayE visit(latte.Absyn.ArrAccess p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.ArrAccess(expr_1, expr_2);
    }
/* FieldE */
    public FieldE visit(latte.Absyn.FldAccess p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      String ident_ = p.ident_;
      return new latte.Absyn.FldAccess(expr_, ident_);
    }
/* Expr */
    public Expr visit(latte.Absyn.IVar p, A arg)
    {
      String ident_ = p.ident_;
      return new latte.Absyn.IVar(ident_);
    }    public Expr visit(latte.Absyn.AVar p, A arg)
    {
      ArrayE arraye_ = p.arraye_.accept(this, arg);
      return new latte.Absyn.AVar(arraye_);
    }    public Expr visit(latte.Absyn.FVar p, A arg)
    {
      FieldE fielde_ = p.fielde_.accept(this, arg);
      return new latte.Absyn.FVar(fielde_);
    }    public Expr visit(latte.Absyn.ELitInt p, A arg)
    {
      Integer integer_ = p.integer_;
      return new latte.Absyn.ELitInt(integer_);
    }    public Expr visit(latte.Absyn.ELitTrue p, A arg)
    {
      return new latte.Absyn.ELitTrue();
    }    public Expr visit(latte.Absyn.ELitFalse p, A arg)
    {
      return new latte.Absyn.ELitFalse();
    }    public Expr visit(latte.Absyn.EApp p, A arg)
    {
      String ident_ = p.ident_;
      ListExpr listexpr_ = new ListExpr();
      for (Expr x : p.listexpr_)
      {
        listexpr_.add(x.accept(this,arg));
      }
      return new latte.Absyn.EApp(ident_, listexpr_);
    }    public Expr visit(latte.Absyn.EMethod p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      String ident_ = p.ident_;
      ListExpr listexpr_ = new ListExpr();
      for (Expr x : p.listexpr_)
      {
        listexpr_.add(x.accept(this,arg));
      }
      return new latte.Absyn.EMethod(expr_, ident_, listexpr_);
    }    public Expr visit(latte.Absyn.EString p, A arg)
    {
      String string_ = p.string_;
      return new latte.Absyn.EString(string_);
    }    public Expr visit(latte.Absyn.EClassCons p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      return new latte.Absyn.EClassCons(type_);
    }    public Expr visit(latte.Absyn.EArrayCons p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.EArrayCons(type_, expr_);
    }    public Expr visit(latte.Absyn.ECast p, A arg)
    {
      Type type_ = p.type_.accept(this, arg);
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.ECast(type_, expr_);
    }    public Expr visit(latte.Absyn.Neg p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.Neg(expr_);
    }    public Expr visit(latte.Absyn.Not p, A arg)
    {
      Expr expr_ = p.expr_.accept(this, arg);
      return new latte.Absyn.Not(expr_);
    }    public Expr visit(latte.Absyn.EMul p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      MulOp mulop_ = p.mulop_.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.EMul(expr_1, mulop_, expr_2);
    }    public Expr visit(latte.Absyn.EAdd p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      AddOp addop_ = p.addop_.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.EAdd(expr_1, addop_, expr_2);
    }    public Expr visit(latte.Absyn.ERel p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      RelOp relop_ = p.relop_.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.ERel(expr_1, relop_, expr_2);
    }    public Expr visit(latte.Absyn.EAnd p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.EAnd(expr_1, expr_2);
    }    public Expr visit(latte.Absyn.EOr p, A arg)
    {
      Expr expr_1 = p.expr_1.accept(this, arg);
      Expr expr_2 = p.expr_2.accept(this, arg);
      return new latte.Absyn.EOr(expr_1, expr_2);
    }
/* AddOp */
    public AddOp visit(latte.Absyn.Plus p, A arg)
    {
      return new latte.Absyn.Plus();
    }    public AddOp visit(latte.Absyn.Minus p, A arg)
    {
      return new latte.Absyn.Minus();
    }
/* MulOp */
    public MulOp visit(latte.Absyn.Times p, A arg)
    {
      return new latte.Absyn.Times();
    }    public MulOp visit(latte.Absyn.Div p, A arg)
    {
      return new latte.Absyn.Div();
    }    public MulOp visit(latte.Absyn.Mod p, A arg)
    {
      return new latte.Absyn.Mod();
    }
/* RelOp */
    public RelOp visit(latte.Absyn.LTH p, A arg)
    {
      return new latte.Absyn.LTH();
    }    public RelOp visit(latte.Absyn.LE p, A arg)
    {
      return new latte.Absyn.LE();
    }    public RelOp visit(latte.Absyn.GTH p, A arg)
    {
      return new latte.Absyn.GTH();
    }    public RelOp visit(latte.Absyn.GE p, A arg)
    {
      return new latte.Absyn.GE();
    }    public RelOp visit(latte.Absyn.EQU p, A arg)
    {
      return new latte.Absyn.EQU();
    }    public RelOp visit(latte.Absyn.NE p, A arg)
    {
      return new latte.Absyn.NE();
    }
}