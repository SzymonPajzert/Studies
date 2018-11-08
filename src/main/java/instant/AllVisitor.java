package instant;

import instant.Absyn.*;

/** BNFC-Generated All Visitor */
public interface AllVisitor<R,A> extends
  instant.Absyn.Program.Visitor<R,A>,
  instant.Absyn.Stmt.Visitor<R,A>,
  instant.Absyn.Exp.Visitor<R,A>
{}
