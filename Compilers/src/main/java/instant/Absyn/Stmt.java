package instant.Absyn; // Java Package generated by the BNF Converter.

public abstract class Stmt implements java.io.Serializable {
  public abstract <R,A> R accept(Stmt.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(instant.Absyn.SAss p, A arg);
    public R visit(instant.Absyn.SExp p, A arg);

  }

}
