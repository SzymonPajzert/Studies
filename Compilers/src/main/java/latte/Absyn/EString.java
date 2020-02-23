package latte.Absyn; // Java Package generated by the BNF Converter.

public class EString extends Expr {
  public final String string_;
  public EString(String p1) { string_ = p1; }

  public <R,A> R accept(latte.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof latte.Absyn.EString) {
      latte.Absyn.EString x = (latte.Absyn.EString)o;
      return this.string_.equals(x.string_);
    }
    return false;
  }

  public int hashCode() {
    return this.string_.hashCode();
  }


}