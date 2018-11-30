package latte;
import latte.Absyn.*;
/*** BNFC-Generated Visitor Design Pattern Skeleton. ***/
/* This implements the common visitor design pattern.
   Tests show it to be slightly less efficient than the
   instanceof method, but easier to use. 
   Replace the R and A parameters with the desired return
   and context types.*/

public class VisitSkel
{
  public class ProgramVisitor<R,A> implements Program.Visitor<R,A>
  {
    public R visit(latte.Absyn.ProgramCons p, A arg)
    { /* Code For ProgramCons Goes Here */
      for (TopDef x: p.listtopdef_)
      { /* ... */ }
      return null;
    }
  }
  public class TopDefVisitor<R,A> implements TopDef.Visitor<R,A>
  {
    public R visit(latte.Absyn.FnDef p, A arg)
    { /* Code For FnDef Goes Here */
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      //p.ident_;
      for (Arg x: p.listarg_)
      { /* ... */ }
      p.block_.accept(new BlockVisitor<R,A>(), arg);
      return null;
    }
  }
  public class ArgVisitor<R,A> implements Arg.Visitor<R,A>
  {
    public R visit(latte.Absyn.ArgCons p, A arg)
    { /* Code For ArgCons Goes Here */
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      //p.ident_;
      return null;
    }
  }
  public class BlockVisitor<R,A> implements Block.Visitor<R,A>
  {
    public R visit(latte.Absyn.BlockCons p, A arg)
    { /* Code For BlockCons Goes Here */
      for (Stmt x: p.liststmt_)
      { /* ... */ }
      return null;
    }
  }
  public class StmtVisitor<R,A> implements Stmt.Visitor<R,A>
  {
    public R visit(latte.Absyn.Empty p, A arg)
    { /* Code For Empty Goes Here */
      return null;
    }    public R visit(latte.Absyn.BStmt p, A arg)
    { /* Code For BStmt Goes Here */
      p.block_.accept(new BlockVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.Decl p, A arg)
    { /* Code For Decl Goes Here */
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      for (Item x: p.listitem_)
      { /* ... */ }
      return null;
    }    public R visit(latte.Absyn.Ass p, A arg)
    { /* Code For Ass Goes Here */
      //p.ident_;
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.Incr p, A arg)
    { /* Code For Incr Goes Here */
      //p.ident_;
      return null;
    }    public R visit(latte.Absyn.Decr p, A arg)
    { /* Code For Decr Goes Here */
      //p.ident_;
      return null;
    }    public R visit(latte.Absyn.Ret p, A arg)
    { /* Code For Ret Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.VRet p, A arg)
    { /* Code For VRet Goes Here */
      return null;
    }    public R visit(latte.Absyn.Cond p, A arg)
    { /* Code For Cond Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.CondElse p, A arg)
    { /* Code For CondElse Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_1.accept(new StmtVisitor<R,A>(), arg);
      p.stmt_2.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.While p, A arg)
    { /* Code For While Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      p.stmt_.accept(new StmtVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.SExp p, A arg)
    { /* Code For SExp Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
  }
  public class ItemVisitor<R,A> implements Item.Visitor<R,A>
  {
    public R visit(latte.Absyn.NoInit p, A arg)
    { /* Code For NoInit Goes Here */
      //p.ident_;
      return null;
    }    public R visit(latte.Absyn.Init p, A arg)
    { /* Code For Init Goes Here */
      //p.ident_;
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }
  }
  public class TypeVisitor<R,A> implements Type.Visitor<R,A>
  {
    public R visit(latte.Absyn.Int p, A arg)
    { /* Code For Int Goes Here */
      return null;
    }    public R visit(latte.Absyn.Str p, A arg)
    { /* Code For Str Goes Here */
      return null;
    }    public R visit(latte.Absyn.Bool p, A arg)
    { /* Code For Bool Goes Here */
      return null;
    }    public R visit(latte.Absyn.Void p, A arg)
    { /* Code For Void Goes Here */
      return null;
    }    public R visit(latte.Absyn.Fun p, A arg)
    { /* Code For Fun Goes Here */
      p.type_.accept(new TypeVisitor<R,A>(), arg);
      for (Type x: p.listtype_)
      { /* ... */ }
      return null;
    }
  }
  public class ExprVisitor<R,A> implements Expr.Visitor<R,A>
  {
    public R visit(latte.Absyn.EVar p, A arg)
    { /* Code For EVar Goes Here */
      //p.ident_;
      return null;
    }    public R visit(latte.Absyn.ELitInt p, A arg)
    { /* Code For ELitInt Goes Here */
      //p.integer_;
      return null;
    }    public R visit(latte.Absyn.ELitTrue p, A arg)
    { /* Code For ELitTrue Goes Here */
      return null;
    }    public R visit(latte.Absyn.ELitFalse p, A arg)
    { /* Code For ELitFalse Goes Here */
      return null;
    }    public R visit(latte.Absyn.EApp p, A arg)
    { /* Code For EApp Goes Here */
      //p.ident_;
      for (Expr x: p.listexpr_)
      { /* ... */ }
      return null;
    }    public R visit(latte.Absyn.EString p, A arg)
    { /* Code For EString Goes Here */
      //p.string_;
      return null;
    }        public R visit(latte.Absyn.Neg p, A arg)
    { /* Code For Neg Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }    public R visit(latte.Absyn.Not p, A arg)
    { /* Code For Not Goes Here */
      p.expr_.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }        public R visit(latte.Absyn.EMul p, A arg)
    { /* Code For EMul Goes Here */
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.mulop_.accept(new MulOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }        public R visit(latte.Absyn.EAdd p, A arg)
    { /* Code For EAdd Goes Here */
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.addop_.accept(new AddOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }        public R visit(latte.Absyn.ERel p, A arg)
    { /* Code For ERel Goes Here */
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.relop_.accept(new RelOpVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }        public R visit(latte.Absyn.EAnd p, A arg)
    { /* Code For EAnd Goes Here */
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }        public R visit(latte.Absyn.EOr p, A arg)
    { /* Code For EOr Goes Here */
      p.expr_1.accept(new ExprVisitor<R,A>(), arg);
      p.expr_2.accept(new ExprVisitor<R,A>(), arg);
      return null;
    }    
  }
  public class AddOpVisitor<R,A> implements AddOp.Visitor<R,A>
  {
    public R visit(latte.Absyn.Plus p, A arg)
    { /* Code For Plus Goes Here */
      return null;
    }    public R visit(latte.Absyn.Minus p, A arg)
    { /* Code For Minus Goes Here */
      return null;
    }
  }
  public class MulOpVisitor<R,A> implements MulOp.Visitor<R,A>
  {
    public R visit(latte.Absyn.Times p, A arg)
    { /* Code For Times Goes Here */
      return null;
    }    public R visit(latte.Absyn.Div p, A arg)
    { /* Code For Div Goes Here */
      return null;
    }    public R visit(latte.Absyn.Mod p, A arg)
    { /* Code For Mod Goes Here */
      return null;
    }
  }
  public class RelOpVisitor<R,A> implements RelOp.Visitor<R,A>
  {
    public R visit(latte.Absyn.LTH p, A arg)
    { /* Code For LTH Goes Here */
      return null;
    }    public R visit(latte.Absyn.LE p, A arg)
    { /* Code For LE Goes Here */
      return null;
    }    public R visit(latte.Absyn.GTH p, A arg)
    { /* Code For GTH Goes Here */
      return null;
    }    public R visit(latte.Absyn.GE p, A arg)
    { /* Code For GE Goes Here */
      return null;
    }    public R visit(latte.Absyn.EQU p, A arg)
    { /* Code For EQU Goes Here */
      return null;
    }    public R visit(latte.Absyn.NE p, A arg)
    { /* Code For NE Goes Here */
      return null;
    }
  }
}