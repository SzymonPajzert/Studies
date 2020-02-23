package instant;
import instant.Absyn.*;
/** BNFC-Generated Abstract Visitor */
public class AbstractVisitor<R,A> implements AllVisitor<R,A> {
/* Program */
    public R visit(instant.Absyn.Prog p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(instant.Absyn.Program p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Stmt */
    public R visit(instant.Absyn.SAss p, A arg) { return visitDefault(p, arg); }
    public R visit(instant.Absyn.SExp p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(instant.Absyn.Stmt p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Exp */
    public R visit(instant.Absyn.ExpAdd p, A arg) { return visitDefault(p, arg); }

    public R visit(instant.Absyn.ExpSub p, A arg) { return visitDefault(p, arg); }

    public R visit(instant.Absyn.ExpMul p, A arg) { return visitDefault(p, arg); }
    public R visit(instant.Absyn.ExpDiv p, A arg) { return visitDefault(p, arg); }

    public R visit(instant.Absyn.ExpLit p, A arg) { return visitDefault(p, arg); }
    public R visit(instant.Absyn.ExpVar p, A arg) { return visitDefault(p, arg); }


    public R visitDefault(instant.Absyn.Exp p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }

}
