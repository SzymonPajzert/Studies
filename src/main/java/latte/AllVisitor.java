package latte;

import latte.Absyn.*;

/** BNFC-Generated All Visitor */
public interface AllVisitor<R,A> extends
  latte.Absyn.Program.Visitor<R,A>,
  latte.Absyn.TopDef.Visitor<R,A>,
  latte.Absyn.Arg.Visitor<R,A>,
  latte.Absyn.Block.Visitor<R,A>,
  latte.Absyn.Stmt.Visitor<R,A>,
  latte.Absyn.Item.Visitor<R,A>,
  latte.Absyn.Type.Visitor<R,A>,
  latte.Absyn.Expr.Visitor<R,A>,
  latte.Absyn.AddOp.Visitor<R,A>,
  latte.Absyn.MulOp.Visitor<R,A>,
  latte.Absyn.RelOp.Visitor<R,A>
{}
