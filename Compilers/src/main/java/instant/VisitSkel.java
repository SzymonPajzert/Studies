package instant;
import instant.Absyn.*;
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
    public R visit(instant.Absyn.Prog p, A arg)
    { /* Code For Prog Goes Here */
      for (Stmt x: p.liststmt_)
      { /* ... */ }
      return null;
    }
  }
  public class StmtVisitor<R,A> implements Stmt.Visitor<R,A>
  {
    public R visit(instant.Absyn.SAss p, A arg)
    { /* Code For SAss Goes Here */
      //p.ident_;
      p.exp_.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }    public R visit(instant.Absyn.SExp p, A arg)
    { /* Code For SExp Goes Here */
      p.exp_.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }
  }
  public class ExpVisitor<R,A> implements Exp.Visitor<R,A>
  {
    public R visit(instant.Absyn.ExpAdd p, A arg)
    { /* Code For ExpAdd Goes Here */
      p.exp_1.accept(new ExpVisitor<R,A>(), arg);
      p.exp_2.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }        public R visit(instant.Absyn.ExpSub p, A arg)
    { /* Code For ExpSub Goes Here */
      p.exp_1.accept(new ExpVisitor<R,A>(), arg);
      p.exp_2.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }        public R visit(instant.Absyn.ExpMul p, A arg)
    { /* Code For ExpMul Goes Here */
      p.exp_1.accept(new ExpVisitor<R,A>(), arg);
      p.exp_2.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }    public R visit(instant.Absyn.ExpDiv p, A arg)
    { /* Code For ExpDiv Goes Here */
      p.exp_1.accept(new ExpVisitor<R,A>(), arg);
      p.exp_2.accept(new ExpVisitor<R,A>(), arg);
      return null;
    }        public R visit(instant.Absyn.ExpLit p, A arg)
    { /* Code For ExpLit Goes Here */
      //p.integer_;
      return null;
    }    public R visit(instant.Absyn.ExpVar p, A arg)
    { /* Code For ExpVar Goes Here */
      //p.ident_;
      return null;
    }        
  }
}