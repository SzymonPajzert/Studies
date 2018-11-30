package latte.Absyn; // Java Package generated by the BNF Converter.

public abstract class Item implements java.io.Serializable {
  public abstract <R,A> R accept(Item.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(latte.Absyn.NoInit p, A arg);
    public R visit(latte.Absyn.Init p, A arg);

  }

}
