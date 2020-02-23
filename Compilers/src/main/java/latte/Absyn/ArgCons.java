package latte.Absyn; // Java Package generated by the BNF Converter.

public class ArgCons extends Arg {
  public final Type type_;
  public final String ident_;
  public ArgCons(Type p1, String p2) { type_ = p1; ident_ = p2; }

  public <R,A> R accept(latte.Absyn.Arg.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof latte.Absyn.ArgCons) {
      latte.Absyn.ArgCons x = (latte.Absyn.ArgCons)o;
      return this.type_.equals(x.type_) && this.ident_.equals(x.ident_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.type_.hashCode())+this.ident_.hashCode();
  }


}
