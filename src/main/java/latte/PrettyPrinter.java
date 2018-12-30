package latte;
import latte.Absyn.*;

public class PrettyPrinter
{
  //For certain applications increasing the initial size of the buffer may improve performance.
  private static final int INITIAL_BUFFER_SIZE = 128;
  private static final int INDENT_WIDTH = 2;
  //You may wish to change the parentheses used in precedence.
  private static final String _L_PAREN = new String("(");
  private static final String _R_PAREN = new String(")");
  //You may wish to change render
  private static void render(String s)
  {
    if (s.equals("{"))
    {
       buf_.append("\n");
       indent();
       buf_.append(s);
       _n_ = _n_ + INDENT_WIDTH;
       buf_.append("\n");
       indent();
    }
    else if (s.equals("(") || s.equals("["))
       buf_.append(s);
    else if (s.equals(")") || s.equals("]"))
    {
       backup();
       buf_.append(s);
       buf_.append(" ");
    }
    else if (s.equals("}"))
    {
       int t;
       _n_ = _n_ - INDENT_WIDTH;
       for(t=0; t<INDENT_WIDTH; t++) {
         backup();
       }
       buf_.append(s);
       buf_.append("\n");
       indent();
    }
    else if (s.equals(","))
    {
       backup();
       buf_.append(s);
       buf_.append(" ");
    }
    else if (s.equals(";"))
    {
       backup();
       buf_.append(s);
       buf_.append("\n");
       indent();
    }
    else if (s.equals("")) return;
    else
    {
       buf_.append(s);
       buf_.append(" ");
    }
  }


  //  print and show methods are defined for each category.
  public static String print(latte.Absyn.Program foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Program foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.TopDef foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.TopDef foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListTopDef foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListTopDef foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Arg foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Arg foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListArg foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListArg foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Block foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Block foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListStmt foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListStmt foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Stmt foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Stmt foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Item foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Item foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListItem foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListItem foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Type foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Type foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListType foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListType foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.Expr foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.Expr foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.ListExpr foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.ListExpr foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.AddOp foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.AddOp foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.MulOp foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.MulOp foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(latte.Absyn.RelOp foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(latte.Absyn.RelOp foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  /***   You shouldn't need to change anything beyond this point.   ***/

  private static void pp(latte.Absyn.Program foo, int _i_)
  {
    if (foo instanceof latte.Absyn.ProgramCons)
    {
       latte.Absyn.ProgramCons _programcons = (latte.Absyn.ProgramCons) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_programcons.listtopdef_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.TopDef foo, int _i_)
  {
    if (foo instanceof latte.Absyn.FnDef)
    {
       latte.Absyn.FnDef _fndef = (latte.Absyn.FnDef) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_fndef.type_, 0);
       pp(_fndef.ident_, 0);
       render("(");
       pp(_fndef.listarg_, 0);
       render(")");
       pp(_fndef.block_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListTopDef foo, int _i_)
  {
     for (java.util.Iterator<TopDef> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render("");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.Arg foo, int _i_)
  {
    if (foo instanceof latte.Absyn.ArgCons)
    {
       latte.Absyn.ArgCons _argcons = (latte.Absyn.ArgCons) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_argcons.type_, 0);
       pp(_argcons.ident_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListArg foo, int _i_)
  {
     for (java.util.Iterator<Arg> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.Block foo, int _i_)
  {
    if (foo instanceof latte.Absyn.BlockCons)
    {
       latte.Absyn.BlockCons _blockcons = (latte.Absyn.BlockCons) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("{");
       pp(_blockcons.liststmt_, 0);
       render("}");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListStmt foo, int _i_)
  {
     for (java.util.Iterator<Stmt> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render("");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.Stmt foo, int _i_)
  {
    if (foo instanceof latte.Absyn.Empty)
    {
       latte.Absyn.Empty _empty = (latte.Absyn.Empty) foo;
       if (_i_ > 0) render(_L_PAREN);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.BStmt)
    {
       latte.Absyn.BStmt _bstmt = (latte.Absyn.BStmt) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_bstmt.block_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Decl)
    {
       latte.Absyn.Decl _decl = (latte.Absyn.Decl) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_decl.type_, 0);
       pp(_decl.listitem_, 0);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Ass)
    {
       latte.Absyn.Ass _ass = (latte.Absyn.Ass) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_ass.ident_, 0);
       render("=");
       pp(_ass.expr_, 0);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.AssArr)
    {
       latte.Absyn.AssArr _assarr = (latte.Absyn.AssArr) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_assarr.ident_, 0);
       render("[");
       pp(_assarr.expr_1, 0);
       render("]");
       render("=");
       pp(_assarr.expr_2, 0);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Incr)
    {
       latte.Absyn.Incr _incr = (latte.Absyn.Incr) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_incr.ident_, 0);
       render("++");
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Decr)
    {
       latte.Absyn.Decr _decr = (latte.Absyn.Decr) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_decr.ident_, 0);
       render("--");
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Ret)
    {
       latte.Absyn.Ret _ret = (latte.Absyn.Ret) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("return");
       pp(_ret.expr_, 0);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.VRet)
    {
       latte.Absyn.VRet _vret = (latte.Absyn.VRet) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("return");
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Cond)
    {
       latte.Absyn.Cond _cond = (latte.Absyn.Cond) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("if");
       render("(");
       pp(_cond.expr_, 0);
       render(")");
       pp(_cond.stmt_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.CondElse)
    {
       latte.Absyn.CondElse _condelse = (latte.Absyn.CondElse) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("if");
       render("(");
       pp(_condelse.expr_, 0);
       render(")");
       pp(_condelse.stmt_1, 0);
       render("else");
       pp(_condelse.stmt_2, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.While)
    {
       latte.Absyn.While _while = (latte.Absyn.While) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("while");
       render("(");
       pp(_while.expr_, 0);
       render(")");
       pp(_while.stmt_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.For)
    {
       latte.Absyn.For _for = (latte.Absyn.For) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("for");
       render("(");
       pp(_for.stmt_1, 0);
       pp(_for.expr_, 0);
       render(";");
       pp(_for.stmt_2, 0);
       render(")");
       pp(_for.stmt_3, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.SExp)
    {
       latte.Absyn.SExp _sexp = (latte.Absyn.SExp) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sexp.expr_, 0);
       render(";");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.Item foo, int _i_)
  {
    if (foo instanceof latte.Absyn.NoInit)
    {
       latte.Absyn.NoInit _noinit = (latte.Absyn.NoInit) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_noinit.ident_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Init)
    {
       latte.Absyn.Init _init = (latte.Absyn.Init) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_init.ident_, 0);
       render("=");
       pp(_init.expr_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListItem foo, int _i_)
  {
     for (java.util.Iterator<Item> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.Type foo, int _i_)
  {
    if (foo instanceof latte.Absyn.Int)
    {
       latte.Absyn.Int _int = (latte.Absyn.Int) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("int");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Str)
    {
       latte.Absyn.Str _str = (latte.Absyn.Str) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("string");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Bool)
    {
       latte.Absyn.Bool _bool = (latte.Absyn.Bool) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("boolean");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Void)
    {
       latte.Absyn.Void _void = (latte.Absyn.Void) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("void");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Fun)
    {
       latte.Absyn.Fun _fun = (latte.Absyn.Fun) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_fun.type_, 0);
       render("(");
       pp(_fun.listtype_, 0);
       render(")");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.ArrayT)
    {
       latte.Absyn.ArrayT _arrayt = (latte.Absyn.ArrayT) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_arrayt.type_, 0);
       render("[]");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListType foo, int _i_)
  {
     for (java.util.Iterator<Type> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.Expr foo, int _i_)
  {
    if (foo instanceof latte.Absyn.EVar)
    {
       latte.Absyn.EVar _evar = (latte.Absyn.EVar) foo;
       if (_i_ > 6) render(_L_PAREN);
       pp(_evar.ident_, 0);
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.ELitInt)
    {
       latte.Absyn.ELitInt _elitint = (latte.Absyn.ELitInt) foo;
       if (_i_ > 6) render(_L_PAREN);
       pp(_elitint.integer_, 0);
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.ELitTrue)
    {
       latte.Absyn.ELitTrue _elittrue = (latte.Absyn.ELitTrue) foo;
       if (_i_ > 6) render(_L_PAREN);
       render("true");
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.ELitFalse)
    {
       latte.Absyn.ELitFalse _elitfalse = (latte.Absyn.ELitFalse) foo;
       if (_i_ > 6) render(_L_PAREN);
       render("false");
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EArrAcc)
    {
       latte.Absyn.EArrAcc _earracc = (latte.Absyn.EArrAcc) foo;
       if (_i_ > 6) render(_L_PAREN);
       pp(_earracc.expr_1, 0);
       render("[");
       pp(_earracc.expr_2, 0);
       render("]");
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EApp)
    {
       latte.Absyn.EApp _eapp = (latte.Absyn.EApp) foo;
       if (_i_ > 6) render(_L_PAREN);
       pp(_eapp.ident_, 0);
       render("(");
       pp(_eapp.listexpr_, 0);
       render(")");
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EString)
    {
       latte.Absyn.EString _estring = (latte.Absyn.EString) foo;
       if (_i_ > 6) render(_L_PAREN);
       pp(_estring.string_, 0);
       if (_i_ > 6) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EArrayCons)
    {
       latte.Absyn.EArrayCons _earraycons = (latte.Absyn.EArrayCons) foo;
       if (_i_ > 5) render(_L_PAREN);
       render("new");
       pp(_earraycons.type_, 0);
       render("[");
       pp(_earraycons.integer_, 0);
       render("]");
       if (_i_ > 5) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Neg)
    {
       latte.Absyn.Neg _neg = (latte.Absyn.Neg) foo;
       if (_i_ > 5) render(_L_PAREN);
       render("-");
       pp(_neg.expr_, 6);
       if (_i_ > 5) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Not)
    {
       latte.Absyn.Not _not = (latte.Absyn.Not) foo;
       if (_i_ > 5) render(_L_PAREN);
       render("!");
       pp(_not.expr_, 6);
       if (_i_ > 5) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EMul)
    {
       latte.Absyn.EMul _emul = (latte.Absyn.EMul) foo;
       if (_i_ > 4) render(_L_PAREN);
       pp(_emul.expr_1, 4);
       pp(_emul.mulop_, 0);
       pp(_emul.expr_2, 5);
       if (_i_ > 4) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EAdd)
    {
       latte.Absyn.EAdd _eadd = (latte.Absyn.EAdd) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_eadd.expr_1, 3);
       pp(_eadd.addop_, 0);
       pp(_eadd.expr_2, 4);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.ERel)
    {
       latte.Absyn.ERel _erel = (latte.Absyn.ERel) foo;
       if (_i_ > 2) render(_L_PAREN);
       pp(_erel.expr_1, 2);
       pp(_erel.relop_, 0);
       pp(_erel.expr_2, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EAnd)
    {
       latte.Absyn.EAnd _eand = (latte.Absyn.EAnd) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_eand.expr_1, 2);
       render("&&");
       pp(_eand.expr_2, 1);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EOr)
    {
       latte.Absyn.EOr _eor = (latte.Absyn.EOr) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_eor.expr_1, 1);
       render("||");
       pp(_eor.expr_2, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.ListExpr foo, int _i_)
  {
     for (java.util.Iterator<Expr> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }  }

  private static void pp(latte.Absyn.AddOp foo, int _i_)
  {
    if (foo instanceof latte.Absyn.Plus)
    {
       latte.Absyn.Plus _plus = (latte.Absyn.Plus) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("+");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Minus)
    {
       latte.Absyn.Minus _minus = (latte.Absyn.Minus) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("-");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.MulOp foo, int _i_)
  {
    if (foo instanceof latte.Absyn.Times)
    {
       latte.Absyn.Times _times = (latte.Absyn.Times) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("*");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Div)
    {
       latte.Absyn.Div _div = (latte.Absyn.Div) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("/");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.Mod)
    {
       latte.Absyn.Mod _mod = (latte.Absyn.Mod) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("%");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(latte.Absyn.RelOp foo, int _i_)
  {
    if (foo instanceof latte.Absyn.LTH)
    {
       latte.Absyn.LTH _lth = (latte.Absyn.LTH) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("<");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.LE)
    {
       latte.Absyn.LE _le = (latte.Absyn.LE) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("<=");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.GTH)
    {
       latte.Absyn.GTH _gth = (latte.Absyn.GTH) foo;
       if (_i_ > 0) render(_L_PAREN);
       render(">");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.GE)
    {
       latte.Absyn.GE _ge = (latte.Absyn.GE) foo;
       if (_i_ > 0) render(_L_PAREN);
       render(">=");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.EQU)
    {
       latte.Absyn.EQU _equ = (latte.Absyn.EQU) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("==");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof latte.Absyn.NE)
    {
       latte.Absyn.NE _ne = (latte.Absyn.NE) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("!=");
       if (_i_ > 0) render(_R_PAREN);
    }
  }


  private static void sh(latte.Absyn.Program foo)
  {
    if (foo instanceof latte.Absyn.ProgramCons)
    {
       latte.Absyn.ProgramCons _programcons = (latte.Absyn.ProgramCons) foo;
       render("(");
       render("ProgramCons");
       render("[");
       sh(_programcons.listtopdef_);
       render("]");
       render(")");
    }
  }

  private static void sh(latte.Absyn.TopDef foo)
  {
    if (foo instanceof latte.Absyn.FnDef)
    {
       latte.Absyn.FnDef _fndef = (latte.Absyn.FnDef) foo;
       render("(");
       render("FnDef");
       sh(_fndef.type_);
       sh(_fndef.ident_);
       render("[");
       sh(_fndef.listarg_);
       render("]");
       sh(_fndef.block_);
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListTopDef foo)
  {
     for (java.util.Iterator<TopDef> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.Arg foo)
  {
    if (foo instanceof latte.Absyn.ArgCons)
    {
       latte.Absyn.ArgCons _argcons = (latte.Absyn.ArgCons) foo;
       render("(");
       render("ArgCons");
       sh(_argcons.type_);
       sh(_argcons.ident_);
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListArg foo)
  {
     for (java.util.Iterator<Arg> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.Block foo)
  {
    if (foo instanceof latte.Absyn.BlockCons)
    {
       latte.Absyn.BlockCons _blockcons = (latte.Absyn.BlockCons) foo;
       render("(");
       render("BlockCons");
       render("[");
       sh(_blockcons.liststmt_);
       render("]");
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListStmt foo)
  {
     for (java.util.Iterator<Stmt> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.Stmt foo)
  {
    if (foo instanceof latte.Absyn.Empty)
    {
       latte.Absyn.Empty _empty = (latte.Absyn.Empty) foo;
       render("Empty");
    }
    if (foo instanceof latte.Absyn.BStmt)
    {
       latte.Absyn.BStmt _bstmt = (latte.Absyn.BStmt) foo;
       render("(");
       render("BStmt");
       sh(_bstmt.block_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Decl)
    {
       latte.Absyn.Decl _decl = (latte.Absyn.Decl) foo;
       render("(");
       render("Decl");
       sh(_decl.type_);
       render("[");
       sh(_decl.listitem_);
       render("]");
       render(")");
    }
    if (foo instanceof latte.Absyn.Ass)
    {
       latte.Absyn.Ass _ass = (latte.Absyn.Ass) foo;
       render("(");
       render("Ass");
       sh(_ass.ident_);
       sh(_ass.expr_);
       render(")");
    }
    if (foo instanceof latte.Absyn.AssArr)
    {
       latte.Absyn.AssArr _assarr = (latte.Absyn.AssArr) foo;
       render("(");
       render("AssArr");
       sh(_assarr.ident_);
       sh(_assarr.expr_1);
       sh(_assarr.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.Incr)
    {
       latte.Absyn.Incr _incr = (latte.Absyn.Incr) foo;
       render("(");
       render("Incr");
       sh(_incr.ident_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Decr)
    {
       latte.Absyn.Decr _decr = (latte.Absyn.Decr) foo;
       render("(");
       render("Decr");
       sh(_decr.ident_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Ret)
    {
       latte.Absyn.Ret _ret = (latte.Absyn.Ret) foo;
       render("(");
       render("Ret");
       sh(_ret.expr_);
       render(")");
    }
    if (foo instanceof latte.Absyn.VRet)
    {
       latte.Absyn.VRet _vret = (latte.Absyn.VRet) foo;
       render("VRet");
    }
    if (foo instanceof latte.Absyn.Cond)
    {
       latte.Absyn.Cond _cond = (latte.Absyn.Cond) foo;
       render("(");
       render("Cond");
       sh(_cond.expr_);
       sh(_cond.stmt_);
       render(")");
    }
    if (foo instanceof latte.Absyn.CondElse)
    {
       latte.Absyn.CondElse _condelse = (latte.Absyn.CondElse) foo;
       render("(");
       render("CondElse");
       sh(_condelse.expr_);
       sh(_condelse.stmt_1);
       sh(_condelse.stmt_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.While)
    {
       latte.Absyn.While _while = (latte.Absyn.While) foo;
       render("(");
       render("While");
       sh(_while.expr_);
       sh(_while.stmt_);
       render(")");
    }
    if (foo instanceof latte.Absyn.For)
    {
       latte.Absyn.For _for = (latte.Absyn.For) foo;
       render("(");
       render("For");
       sh(_for.stmt_1);
       sh(_for.expr_);
       sh(_for.stmt_2);
       sh(_for.stmt_3);
       render(")");
    }
    if (foo instanceof latte.Absyn.SExp)
    {
       latte.Absyn.SExp _sexp = (latte.Absyn.SExp) foo;
       render("(");
       render("SExp");
       sh(_sexp.expr_);
       render(")");
    }
  }

  private static void sh(latte.Absyn.Item foo)
  {
    if (foo instanceof latte.Absyn.NoInit)
    {
       latte.Absyn.NoInit _noinit = (latte.Absyn.NoInit) foo;
       render("(");
       render("NoInit");
       sh(_noinit.ident_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Init)
    {
       latte.Absyn.Init _init = (latte.Absyn.Init) foo;
       render("(");
       render("Init");
       sh(_init.ident_);
       sh(_init.expr_);
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListItem foo)
  {
     for (java.util.Iterator<Item> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.Type foo)
  {
    if (foo instanceof latte.Absyn.Int)
    {
       latte.Absyn.Int _int = (latte.Absyn.Int) foo;
       render("Int");
    }
    if (foo instanceof latte.Absyn.Str)
    {
       latte.Absyn.Str _str = (latte.Absyn.Str) foo;
       render("Str");
    }
    if (foo instanceof latte.Absyn.Bool)
    {
       latte.Absyn.Bool _bool = (latte.Absyn.Bool) foo;
       render("Bool");
    }
    if (foo instanceof latte.Absyn.Void)
    {
       latte.Absyn.Void _void = (latte.Absyn.Void) foo;
       render("Void");
    }
    if (foo instanceof latte.Absyn.Fun)
    {
       latte.Absyn.Fun _fun = (latte.Absyn.Fun) foo;
       render("(");
       render("Fun");
       sh(_fun.type_);
       render("[");
       sh(_fun.listtype_);
       render("]");
       render(")");
    }
    if (foo instanceof latte.Absyn.ArrayT)
    {
       latte.Absyn.ArrayT _arrayt = (latte.Absyn.ArrayT) foo;
       render("(");
       render("ArrayT");
       sh(_arrayt.type_);
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListType foo)
  {
     for (java.util.Iterator<Type> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.Expr foo)
  {
    if (foo instanceof latte.Absyn.EVar)
    {
       latte.Absyn.EVar _evar = (latte.Absyn.EVar) foo;
       render("(");
       render("EVar");
       sh(_evar.ident_);
       render(")");
    }
    if (foo instanceof latte.Absyn.ELitInt)
    {
       latte.Absyn.ELitInt _elitint = (latte.Absyn.ELitInt) foo;
       render("(");
       render("ELitInt");
       sh(_elitint.integer_);
       render(")");
    }
    if (foo instanceof latte.Absyn.ELitTrue)
    {
       latte.Absyn.ELitTrue _elittrue = (latte.Absyn.ELitTrue) foo;
       render("ELitTrue");
    }
    if (foo instanceof latte.Absyn.ELitFalse)
    {
       latte.Absyn.ELitFalse _elitfalse = (latte.Absyn.ELitFalse) foo;
       render("ELitFalse");
    }
    if (foo instanceof latte.Absyn.EArrAcc)
    {
       latte.Absyn.EArrAcc _earracc = (latte.Absyn.EArrAcc) foo;
       render("(");
       render("EArrAcc");
       sh(_earracc.expr_1);
       sh(_earracc.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.EApp)
    {
       latte.Absyn.EApp _eapp = (latte.Absyn.EApp) foo;
       render("(");
       render("EApp");
       sh(_eapp.ident_);
       render("[");
       sh(_eapp.listexpr_);
       render("]");
       render(")");
    }
    if (foo instanceof latte.Absyn.EString)
    {
       latte.Absyn.EString _estring = (latte.Absyn.EString) foo;
       render("(");
       render("EString");
       sh(_estring.string_);
       render(")");
    }
    if (foo instanceof latte.Absyn.EArrayCons)
    {
       latte.Absyn.EArrayCons _earraycons = (latte.Absyn.EArrayCons) foo;
       render("(");
       render("EArrayCons");
       sh(_earraycons.type_);
       sh(_earraycons.integer_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Neg)
    {
       latte.Absyn.Neg _neg = (latte.Absyn.Neg) foo;
       render("(");
       render("Neg");
       sh(_neg.expr_);
       render(")");
    }
    if (foo instanceof latte.Absyn.Not)
    {
       latte.Absyn.Not _not = (latte.Absyn.Not) foo;
       render("(");
       render("Not");
       sh(_not.expr_);
       render(")");
    }
    if (foo instanceof latte.Absyn.EMul)
    {
       latte.Absyn.EMul _emul = (latte.Absyn.EMul) foo;
       render("(");
       render("EMul");
       sh(_emul.expr_1);
       sh(_emul.mulop_);
       sh(_emul.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.EAdd)
    {
       latte.Absyn.EAdd _eadd = (latte.Absyn.EAdd) foo;
       render("(");
       render("EAdd");
       sh(_eadd.expr_1);
       sh(_eadd.addop_);
       sh(_eadd.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.ERel)
    {
       latte.Absyn.ERel _erel = (latte.Absyn.ERel) foo;
       render("(");
       render("ERel");
       sh(_erel.expr_1);
       sh(_erel.relop_);
       sh(_erel.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.EAnd)
    {
       latte.Absyn.EAnd _eand = (latte.Absyn.EAnd) foo;
       render("(");
       render("EAnd");
       sh(_eand.expr_1);
       sh(_eand.expr_2);
       render(")");
    }
    if (foo instanceof latte.Absyn.EOr)
    {
       latte.Absyn.EOr _eor = (latte.Absyn.EOr) foo;
       render("(");
       render("EOr");
       sh(_eor.expr_1);
       sh(_eor.expr_2);
       render(")");
    }
  }

  private static void sh(latte.Absyn.ListExpr foo)
  {
     for (java.util.Iterator<Expr> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(latte.Absyn.AddOp foo)
  {
    if (foo instanceof latte.Absyn.Plus)
    {
       latte.Absyn.Plus _plus = (latte.Absyn.Plus) foo;
       render("Plus");
    }
    if (foo instanceof latte.Absyn.Minus)
    {
       latte.Absyn.Minus _minus = (latte.Absyn.Minus) foo;
       render("Minus");
    }
  }

  private static void sh(latte.Absyn.MulOp foo)
  {
    if (foo instanceof latte.Absyn.Times)
    {
       latte.Absyn.Times _times = (latte.Absyn.Times) foo;
       render("Times");
    }
    if (foo instanceof latte.Absyn.Div)
    {
       latte.Absyn.Div _div = (latte.Absyn.Div) foo;
       render("Div");
    }
    if (foo instanceof latte.Absyn.Mod)
    {
       latte.Absyn.Mod _mod = (latte.Absyn.Mod) foo;
       render("Mod");
    }
  }

  private static void sh(latte.Absyn.RelOp foo)
  {
    if (foo instanceof latte.Absyn.LTH)
    {
       latte.Absyn.LTH _lth = (latte.Absyn.LTH) foo;
       render("LTH");
    }
    if (foo instanceof latte.Absyn.LE)
    {
       latte.Absyn.LE _le = (latte.Absyn.LE) foo;
       render("LE");
    }
    if (foo instanceof latte.Absyn.GTH)
    {
       latte.Absyn.GTH _gth = (latte.Absyn.GTH) foo;
       render("GTH");
    }
    if (foo instanceof latte.Absyn.GE)
    {
       latte.Absyn.GE _ge = (latte.Absyn.GE) foo;
       render("GE");
    }
    if (foo instanceof latte.Absyn.EQU)
    {
       latte.Absyn.EQU _equ = (latte.Absyn.EQU) foo;
       render("EQU");
    }
    if (foo instanceof latte.Absyn.NE)
    {
       latte.Absyn.NE _ne = (latte.Absyn.NE) foo;
       render("NE");
    }
  }


  private static void pp(Integer n, int _i_) { buf_.append(n); buf_.append(" "); }
  private static void pp(Double d, int _i_) { buf_.append(d); buf_.append(" "); }
  private static void pp(String s, int _i_) { buf_.append(s); buf_.append(" "); }
  private static void pp(Character c, int _i_) { buf_.append("'" + c.toString() + "'"); buf_.append(" "); }
  private static void sh(Integer n) { render(n.toString()); }
  private static void sh(Double d) { render(d.toString()); }
  private static void sh(Character c) { render(c.toString()); }
  private static void sh(String s) { printQuoted(s); }
  private static void printQuoted(String s) { render("\"" + s + "\""); }
  private static void indent()
  {
    int n = _n_;
    while (n > 0)
    {
      buf_.append(" ");
      n--;
    }
  }
  private static void backup()
  {
     if (buf_.charAt(buf_.length() - 1) == ' ') {
      buf_.setLength(buf_.length() - 1);
    }
  }
  private static void trim()
  {
     while (buf_.length() > 0 && buf_.charAt(0) == ' ')
        buf_.deleteCharAt(0); 
    while (buf_.length() > 0 && buf_.charAt(buf_.length()-1) == ' ')
        buf_.deleteCharAt(buf_.length()-1);
  }
  private static int _n_ = 0;
  private static StringBuilder buf_ = new StringBuilder(INITIAL_BUFFER_SIZE);
}

