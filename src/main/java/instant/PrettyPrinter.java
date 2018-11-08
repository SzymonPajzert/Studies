package instant;
import instant.Absyn.*;

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
  public static String print(instant.Absyn.Program foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(instant.Absyn.Program foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(instant.Absyn.Stmt foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(instant.Absyn.Stmt foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(instant.Absyn.ListStmt foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(instant.Absyn.ListStmt foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(instant.Absyn.Exp foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(instant.Absyn.Exp foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  /***   You shouldn't need to change anything beyond this point.   ***/

  private static void pp(instant.Absyn.Program foo, int _i_)
  {
    if (foo instanceof instant.Absyn.Prog)
    {
       instant.Absyn.Prog _prog = (instant.Absyn.Prog) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_prog.liststmt_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(instant.Absyn.Stmt foo, int _i_)
  {
    if (foo instanceof instant.Absyn.SAss)
    {
       instant.Absyn.SAss _sass = (instant.Absyn.SAss) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sass.ident_, 0);
       render("=");
       pp(_sass.exp_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.SExp)
    {
       instant.Absyn.SExp _sexp = (instant.Absyn.SExp) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sexp.exp_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(instant.Absyn.ListStmt foo, int _i_)
  {
     for (java.util.Iterator<Stmt> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), _i_);
       if (it.hasNext()) {
         render(";");
       } else {
         render("");
       }
     }  }

  private static void pp(instant.Absyn.Exp foo, int _i_)
  {
    if (foo instanceof instant.Absyn.ExpAdd)
    {
       instant.Absyn.ExpAdd _expadd = (instant.Absyn.ExpAdd) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_expadd.exp_1, 2);
       render("+");
       pp(_expadd.exp_2, 1);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.ExpSub)
    {
       instant.Absyn.ExpSub _expsub = (instant.Absyn.ExpSub) foo;
       if (_i_ > 2) render(_L_PAREN);
       pp(_expsub.exp_1, 2);
       render("-");
       pp(_expsub.exp_2, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.ExpMul)
    {
       instant.Absyn.ExpMul _expmul = (instant.Absyn.ExpMul) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_expmul.exp_1, 3);
       render("*");
       pp(_expmul.exp_2, 4);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.ExpDiv)
    {
       instant.Absyn.ExpDiv _expdiv = (instant.Absyn.ExpDiv) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_expdiv.exp_1, 3);
       render("/");
       pp(_expdiv.exp_2, 4);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.ExpLit)
    {
       instant.Absyn.ExpLit _explit = (instant.Absyn.ExpLit) foo;
       if (_i_ > 4) render(_L_PAREN);
       pp(_explit.integer_, 0);
       if (_i_ > 4) render(_R_PAREN);
    }
    else     if (foo instanceof instant.Absyn.ExpVar)
    {
       instant.Absyn.ExpVar _expvar = (instant.Absyn.ExpVar) foo;
       if (_i_ > 4) render(_L_PAREN);
       pp(_expvar.ident_, 0);
       if (_i_ > 4) render(_R_PAREN);
    }
  }


  private static void sh(instant.Absyn.Program foo)
  {
    if (foo instanceof instant.Absyn.Prog)
    {
       instant.Absyn.Prog _prog = (instant.Absyn.Prog) foo;
       render("(");
       render("Prog");
       render("[");
       sh(_prog.liststmt_);
       render("]");
       render(")");
    }
  }

  private static void sh(instant.Absyn.Stmt foo)
  {
    if (foo instanceof instant.Absyn.SAss)
    {
       instant.Absyn.SAss _sass = (instant.Absyn.SAss) foo;
       render("(");
       render("SAss");
       sh(_sass.ident_);
       sh(_sass.exp_);
       render(")");
    }
    if (foo instanceof instant.Absyn.SExp)
    {
       instant.Absyn.SExp _sexp = (instant.Absyn.SExp) foo;
       render("(");
       render("SExp");
       sh(_sexp.exp_);
       render(")");
    }
  }

  private static void sh(instant.Absyn.ListStmt foo)
  {
     for (java.util.Iterator<Stmt> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(instant.Absyn.Exp foo)
  {
    if (foo instanceof instant.Absyn.ExpAdd)
    {
       instant.Absyn.ExpAdd _expadd = (instant.Absyn.ExpAdd) foo;
       render("(");
       render("ExpAdd");
       sh(_expadd.exp_1);
       sh(_expadd.exp_2);
       render(")");
    }
    if (foo instanceof instant.Absyn.ExpSub)
    {
       instant.Absyn.ExpSub _expsub = (instant.Absyn.ExpSub) foo;
       render("(");
       render("ExpSub");
       sh(_expsub.exp_1);
       sh(_expsub.exp_2);
       render(")");
    }
    if (foo instanceof instant.Absyn.ExpMul)
    {
       instant.Absyn.ExpMul _expmul = (instant.Absyn.ExpMul) foo;
       render("(");
       render("ExpMul");
       sh(_expmul.exp_1);
       sh(_expmul.exp_2);
       render(")");
    }
    if (foo instanceof instant.Absyn.ExpDiv)
    {
       instant.Absyn.ExpDiv _expdiv = (instant.Absyn.ExpDiv) foo;
       render("(");
       render("ExpDiv");
       sh(_expdiv.exp_1);
       sh(_expdiv.exp_2);
       render(")");
    }
    if (foo instanceof instant.Absyn.ExpLit)
    {
       instant.Absyn.ExpLit _explit = (instant.Absyn.ExpLit) foo;
       render("(");
       render("ExpLit");
       sh(_explit.integer_);
       render(")");
    }
    if (foo instanceof instant.Absyn.ExpVar)
    {
       instant.Absyn.ExpVar _expvar = (instant.Absyn.ExpVar) foo;
       render("(");
       render("ExpVar");
       sh(_expvar.ident_);
       render(")");
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

