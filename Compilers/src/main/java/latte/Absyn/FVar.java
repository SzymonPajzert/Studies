package latte.Absyn; // Java Package generated by the BNF Converter.

public class FVar extends Expr {
  public final FieldE fielde_;
  public FVar(FieldE p1) { fielde_ = p1; }

  public <R,A> R accept(latte.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof latte.Absyn.FVar) {
      latte.Absyn.FVar x = (latte.Absyn.FVar)o;
      return this.fielde_.equals(x.fielde_);
    }
    return false;
  }

  public int hashCode() {
    return this.fielde_.hashCode();
  }


}