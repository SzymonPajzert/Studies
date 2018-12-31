JAVAC=javac
JAVAC_FLAGS=-sourcepath .
JAVA=java
JAVA_FLAGS=
CUP=java_cup.Main
CUPFLAGS=-nopositions -expect 100
JLEX=JLex.Main
all: test

test: absyn latte/Yylex.class latte/PrettyPrinter.class latte/Test.class latte/ComposVisitor.class latte/AbstractVisitor.class latte/FoldVisitor.class latte/AllVisitor.class latte/parser.class latte/sym.class latte/Test.class

.PHONY: absyn

%.class: %.java
	${JAVAC} ${JAVAC_FLAGS} $^

absyn: latte/Absyn/Program.java latte/Absyn/ProgramCons.java latte/Absyn/TopDef.java latte/Absyn/FnDef.java latte/Absyn/ClInh.java latte/Absyn/ClDef.java latte/Absyn/ClassElt.java latte/Absyn/Field.java latte/Absyn/Method.java latte/Absyn/ListClassElt.java latte/Absyn/ListTopDef.java latte/Absyn/Arg.java latte/Absyn/ArgCons.java latte/Absyn/ListArg.java latte/Absyn/Block.java latte/Absyn/BlockCons.java latte/Absyn/ListStmt.java latte/Absyn/Stmt.java latte/Absyn/Empty.java latte/Absyn/BStmt.java latte/Absyn/Decl.java latte/Absyn/Ass.java latte/Absyn/AssArr.java latte/Absyn/AssFie.java latte/Absyn/Incr.java latte/Absyn/Decr.java latte/Absyn/Ret.java latte/Absyn/VRet.java latte/Absyn/Cond.java latte/Absyn/CondElse.java latte/Absyn/While.java latte/Absyn/For.java latte/Absyn/ForAbb.java latte/Absyn/SExp.java latte/Absyn/Item.java latte/Absyn/NoInit.java latte/Absyn/Init.java latte/Absyn/ListItem.java latte/Absyn/Type.java latte/Absyn/Int.java latte/Absyn/Str.java latte/Absyn/Bool.java latte/Absyn/Void.java latte/Absyn/Class.java latte/Absyn/Fun.java latte/Absyn/ArrayT.java latte/Absyn/ListType.java latte/Absyn/ArrayE.java latte/Absyn/ArrAccess.java latte/Absyn/FieldE.java latte/Absyn/FldAccess.java latte/Absyn/Expr.java latte/Absyn/IVar.java latte/Absyn/AVar.java latte/Absyn/FVar.java latte/Absyn/ELitInt.java latte/Absyn/ELitTrue.java latte/Absyn/ELitFalse.java latte/Absyn/EApp.java latte/Absyn/EMethod.java latte/Absyn/EString.java latte/Absyn/EClassCons.java latte/Absyn/EArrayCons.java latte/Absyn/ECast.java latte/Absyn/Neg.java latte/Absyn/Not.java latte/Absyn/EMul.java latte/Absyn/EAdd.java latte/Absyn/ERel.java latte/Absyn/EAnd.java latte/Absyn/EOr.java latte/Absyn/ListExpr.java latte/Absyn/AddOp.java latte/Absyn/Plus.java latte/Absyn/Minus.java latte/Absyn/MulOp.java latte/Absyn/Times.java latte/Absyn/Div.java latte/Absyn/Mod.java latte/Absyn/RelOp.java latte/Absyn/LTH.java latte/Absyn/LE.java latte/Absyn/GTH.java latte/Absyn/GE.java latte/Absyn/EQU.java latte/Absyn/NE.java
	${JAVAC} ${JAVAC_FLAGS} $^

latte/Yylex.java: latte/Yylex
	${JAVA} ${JAVA_FLAGS} ${JLEX} latte/Yylex

latte/sym.java latte/parser.java: latte/latte.cup
	${JAVA} ${JAVA_FLAGS} ${CUP} ${CUPFLAGS} latte/latte.cup
	mv sym.java parser.java latte/

latte/Yylex.class: latte/Yylex.java latte/sym.java

latte/sym.class: latte/sym.java

latte/parser.class: latte/parser.java latte/sym.java

latte/PrettyPrinter.class: latte/PrettyPrinter.java

clean:
	rm -f latte/Absyn/*.class latte/*.class

distclean: vclean

vclean:
	 rm -f latte/Absyn/Program.java latte/Absyn/ProgramCons.java latte/Absyn/TopDef.java latte/Absyn/FnDef.java latte/Absyn/ClInh.java latte/Absyn/ClDef.java latte/Absyn/ClassElt.java latte/Absyn/Field.java latte/Absyn/Method.java latte/Absyn/ListClassElt.java latte/Absyn/ListTopDef.java latte/Absyn/Arg.java latte/Absyn/ArgCons.java latte/Absyn/ListArg.java latte/Absyn/Block.java latte/Absyn/BlockCons.java latte/Absyn/ListStmt.java latte/Absyn/Stmt.java latte/Absyn/Empty.java latte/Absyn/BStmt.java latte/Absyn/Decl.java latte/Absyn/Ass.java latte/Absyn/AssArr.java latte/Absyn/AssFie.java latte/Absyn/Incr.java latte/Absyn/Decr.java latte/Absyn/Ret.java latte/Absyn/VRet.java latte/Absyn/Cond.java latte/Absyn/CondElse.java latte/Absyn/While.java latte/Absyn/For.java latte/Absyn/ForAbb.java latte/Absyn/SExp.java latte/Absyn/Item.java latte/Absyn/NoInit.java latte/Absyn/Init.java latte/Absyn/ListItem.java latte/Absyn/Type.java latte/Absyn/Int.java latte/Absyn/Str.java latte/Absyn/Bool.java latte/Absyn/Void.java latte/Absyn/Class.java latte/Absyn/Fun.java latte/Absyn/ArrayT.java latte/Absyn/ListType.java latte/Absyn/ArrayE.java latte/Absyn/ArrAccess.java latte/Absyn/FieldE.java latte/Absyn/FldAccess.java latte/Absyn/Expr.java latte/Absyn/IVar.java latte/Absyn/AVar.java latte/Absyn/FVar.java latte/Absyn/ELitInt.java latte/Absyn/ELitTrue.java latte/Absyn/ELitFalse.java latte/Absyn/EApp.java latte/Absyn/EMethod.java latte/Absyn/EString.java latte/Absyn/EClassCons.java latte/Absyn/EArrayCons.java latte/Absyn/ECast.java latte/Absyn/Neg.java latte/Absyn/Not.java latte/Absyn/EMul.java latte/Absyn/EAdd.java latte/Absyn/ERel.java latte/Absyn/EAnd.java latte/Absyn/EOr.java latte/Absyn/ListExpr.java latte/Absyn/AddOp.java latte/Absyn/Plus.java latte/Absyn/Minus.java latte/Absyn/MulOp.java latte/Absyn/Times.java latte/Absyn/Div.java latte/Absyn/Mod.java latte/Absyn/RelOp.java latte/Absyn/LTH.java latte/Absyn/LE.java latte/Absyn/GTH.java latte/Absyn/GE.java latte/Absyn/EQU.java latte/Absyn/NE.java latte/Absyn/Program.class latte/Absyn/ProgramCons.class latte/Absyn/TopDef.class latte/Absyn/FnDef.class latte/Absyn/ClInh.class latte/Absyn/ClDef.class latte/Absyn/ClassElt.class latte/Absyn/Field.class latte/Absyn/Method.class latte/Absyn/ListClassElt.class latte/Absyn/ListTopDef.class latte/Absyn/Arg.class latte/Absyn/ArgCons.class latte/Absyn/ListArg.class latte/Absyn/Block.class latte/Absyn/BlockCons.class latte/Absyn/ListStmt.class latte/Absyn/Stmt.class latte/Absyn/Empty.class latte/Absyn/BStmt.class latte/Absyn/Decl.class latte/Absyn/Ass.class latte/Absyn/AssArr.class latte/Absyn/AssFie.class latte/Absyn/Incr.class latte/Absyn/Decr.class latte/Absyn/Ret.class latte/Absyn/VRet.class latte/Absyn/Cond.class latte/Absyn/CondElse.class latte/Absyn/While.class latte/Absyn/For.class latte/Absyn/ForAbb.class latte/Absyn/SExp.class latte/Absyn/Item.class latte/Absyn/NoInit.class latte/Absyn/Init.class latte/Absyn/ListItem.class latte/Absyn/Type.class latte/Absyn/Int.class latte/Absyn/Str.class latte/Absyn/Bool.class latte/Absyn/Void.class latte/Absyn/Class.class latte/Absyn/Fun.class latte/Absyn/ArrayT.class latte/Absyn/ListType.class latte/Absyn/ArrayE.class latte/Absyn/ArrAccess.class latte/Absyn/FieldE.class latte/Absyn/FldAccess.class latte/Absyn/Expr.class latte/Absyn/IVar.class latte/Absyn/AVar.class latte/Absyn/FVar.class latte/Absyn/ELitInt.class latte/Absyn/ELitTrue.class latte/Absyn/ELitFalse.class latte/Absyn/EApp.class latte/Absyn/EMethod.class latte/Absyn/EString.class latte/Absyn/EClassCons.class latte/Absyn/EArrayCons.class latte/Absyn/ECast.class latte/Absyn/Neg.class latte/Absyn/Not.class latte/Absyn/EMul.class latte/Absyn/EAdd.class latte/Absyn/ERel.class latte/Absyn/EAnd.class latte/Absyn/EOr.class latte/Absyn/ListExpr.class latte/Absyn/AddOp.class latte/Absyn/Plus.class latte/Absyn/Minus.class latte/Absyn/MulOp.class latte/Absyn/Times.class latte/Absyn/Div.class latte/Absyn/Mod.class latte/Absyn/RelOp.class latte/Absyn/LTH.class latte/Absyn/LE.class latte/Absyn/GTH.class latte/Absyn/GE.class latte/Absyn/EQU.class latte/Absyn/NE.class
	 rm -f latte/Absyn/*.class
	 rmdir latte/Absyn/
	 rm -f latte/Yylex latte/latte.cup latte/Yylex.java latte/VisitSkel.java latte/ComposVisitor.java latte/AbstractVisitor.java latte/FoldVisitor.java latte/AllVisitor.java latte/PrettyPrinter.java latte/Skeleton.java latte/Test.java latte/sym.java latte/parser.java latte/*.class
	rm -f Makefile
	rmdir -p latte/

