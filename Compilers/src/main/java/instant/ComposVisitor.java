package instant;
import instant.Absyn.*;
/** BNFC-Generated Composition Visitor
*/

public class ComposVisitor<A> implements
  instant.Absyn.Program.Visitor<instant.Absyn.Program,A>,
  instant.Absyn.Stmt.Visitor<instant.Absyn.Stmt,A>,
  instant.Absyn.Exp.Visitor<instant.Absyn.Exp,A>
{
/* Program */
    public Program visit(instant.Absyn.Prog p, A arg)
    {
      ListStmt liststmt_ = new ListStmt();
      for (Stmt x : p.liststmt_)
      {
        liststmt_.add(x.accept(this,arg));
      }
      return new instant.Absyn.Prog(liststmt_);
    }
/* Stmt */
    public Stmt visit(instant.Absyn.SAss p, A arg)
    {
      String ident_ = p.ident_;
      Exp exp_ = p.exp_.accept(this, arg);
      return new instant.Absyn.SAss(ident_, exp_);
    }    public Stmt visit(instant.Absyn.SExp p, A arg)
    {
      Exp exp_ = p.exp_.accept(this, arg);
      return new instant.Absyn.SExp(exp_);
    }
/* Exp */
    public Exp visit(instant.Absyn.ExpAdd p, A arg)
    {
      Exp exp_1 = p.exp_1.accept(this, arg);
      Exp exp_2 = p.exp_2.accept(this, arg);
      return new instant.Absyn.ExpAdd(exp_1, exp_2);
    }    public Exp visit(instant.Absyn.ExpSub p, A arg)
    {
      Exp exp_1 = p.exp_1.accept(this, arg);
      Exp exp_2 = p.exp_2.accept(this, arg);
      return new instant.Absyn.ExpSub(exp_1, exp_2);
    }    public Exp visit(instant.Absyn.ExpMul p, A arg)
    {
      Exp exp_1 = p.exp_1.accept(this, arg);
      Exp exp_2 = p.exp_2.accept(this, arg);
      return new instant.Absyn.ExpMul(exp_1, exp_2);
    }    public Exp visit(instant.Absyn.ExpDiv p, A arg)
    {
      Exp exp_1 = p.exp_1.accept(this, arg);
      Exp exp_2 = p.exp_2.accept(this, arg);
      return new instant.Absyn.ExpDiv(exp_1, exp_2);
    }    public Exp visit(instant.Absyn.ExpLit p, A arg)
    {
      Integer integer_ = p.integer_;
      return new instant.Absyn.ExpLit(integer_);
    }    public Exp visit(instant.Absyn.ExpVar p, A arg)
    {
      String ident_ = p.ident_;
      return new instant.Absyn.ExpVar(ident_);
    }
}