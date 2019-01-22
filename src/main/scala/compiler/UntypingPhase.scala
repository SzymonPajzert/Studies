// reviewed: 2018.12.28

package compiler

import language.Type._
import language.{Latte, TypedLatte, UntypedLatte}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

object UntypingPhase extends Compiler[TypedLatte.Code, Latte.Code] {
  type KS = TypedLatte.CodeInformation
  type S[A] = State[KS, A]
  type Compiler[E] = EitherT[S, CompileException, E]

  implicit def ok[A](value: A): Compiler[A] = EitherT[S, CompileException, A](state[KS, CompileException \/ A](\/-(value)))

  implicit def stateToEither[A](value: State[KS, A]): Compiler[A] = {
    EitherT[S, CompileException, A](value map (\/-(_)))
  }

  def mapM[A, B](list: List[A], f: A => Compiler[B]): Compiler[List[B]] =
    (list foldRight (ok(List()): Compiler[List[B]])) { (elt, acc) => for {
        newElt <- f(elt)
        okAcc <- acc
      } yield newElt :: okAcc
    }

  def location: TypedLatte.Location => Compiler[Latte.Location] = {
    case TypedLatte.ArrayAccess((TypedLatte.Variable(name), _), index) =>
      compileExpr(index) map (Latte.ArrayAccess(Latte.Variable(name), _))
    case TypedLatte.Variable(identifier) => Latte.Variable(identifier)

    case TypedLatte.FieldAccess(expressionInf, element) =>
      val className = expressionInf._2 match {
        case c: ClassType => c
        case PointerType(c: ClassType) => c
      }

      (for {
        codeInformation <- get[TypedLatte.CodeInformation]: Compiler[TypedLatte.CodeInformation]
        offset = codeInformation.field(className).offset(element).get
        expression <- compileExpr(expressionInf)
      } yield Latte.FieldAccess(expression, offset)): Compiler[Latte.Location]
  }

  // Reviewed: 2019.01.08
  def compileExpr(expression: TypedLatte.ExpressionInf): Compiler[Latte.Expression] = {
    expression._1 match {
      // Calculate offset and put function type in the tree
      case TypedLatte.FunctionCall((TypedLatte.VTableLookup(exprInfU, ident), _), argumentsU) => for {
        expr <- compileExpr(exprInfU)
        arguments <- mapM(argumentsU.toList, compileExpr)

        classT = exprInfU._2.asInstanceOf[PointerType].deref.asInstanceOf[ClassType]
        offset   <- gets[KS, Int] { _.method(classT).offset(ident).get }            : Compiler[Int]
        funcType <- gets[KS, FunctionType] { _.method(classT).findType(ident).get } : Compiler[FunctionType]
      } yield Latte.FunctionCall(Latte.VTableLookup(expr, offset, funcType), arguments)

      // Nothing interesting below
      case TypedLatte.FunctionCall((TypedLatte.FunName(functionName), _), args) =>
        mapM(args.toList, compileExpr) map (Latte.FunctionCall(Latte.FunName(functionName), _))
      case TypedLatte.ConstValue(v) => Latte.ConstValue(v)
      case TypedLatte.ArrayCreation(a, b) => compileExpr(b) map (Latte.ArrayCreation(a, _))
      case TypedLatte.InstanceCreation(PointerType(classType: ClassType)) =>
        Latte.InstanceCreation(classType: ClassType)
      case locU: TypedLatte.Location => for (loc <- location(locU)) yield loc
      case TypedLatte.Null(_) => Latte.Null(expression._2.deref)
      case TypedLatte.Void => Latte.Void
      case TypedLatte.Cast(PointerType(c: ClassType), (expressionT, PointerType(s: ClassType))) => for {
        expr <- compileExpr((expressionT, PointerType(s)))
      } yield Latte.Cast(PointerType(c), expr)
    }
  }

  def instruction: TypedLatte.Instruction => Compiler[Latte.Instruction] = {
    case TypedLatte.Declaration(name, typeDecl) => Latte.Declaration(name, typeDecl)

    case TypedLatte.Assignment((locU, _), expressionE) => for {
      loc <- location(locU)
      expression <- compileExpr(expressionE)
    } yield Latte.Assignment(loc, expression)

    case TypedLatte.BlockInstruction(instructionsE) => mapM(instructionsE, instruction) map Latte.BlockInstruction

    case TypedLatte.DiscardValue(expression) => compileExpr(expression) map Latte.DiscardValue

    case TypedLatte.Return(value) => value match {
      case None => Latte.Return(None)
      case Some(v) => compileExpr(v) map (vE => Latte.Return(Some(vE)))
    }

    case TypedLatte.IfThen(conditionU, thenInstU, elseOptU) => for {
      condition <- compileExpr(conditionU)
      thenInst <- instruction(thenInstU)
      elseOpt <- (elseOptU match {
        case Some(elseU) => instruction(elseU) map (Some(_))
        case None => None
      }) : Compiler[Option[Latte.Instruction]]
    } yield Latte.IfThen(condition, thenInst, elseOpt)

    case TypedLatte.While(conditionE, instructionsE) => for {
      condition <- compileExpr(conditionE)
      instructions <- instruction(instructionsE)
    } yield Latte.While(condition, instructions)
  }

  def transformSignature(signature: TypedLatte.FunctionSignature): Latte.FunctionSignature =
    Latte.FunctionSignature(signature.identifier, signature.returnType, signature.arguments)

  def compileFunc(func: TypedLatte.TopDefinition): Compiler[Latte.Func] = {
    func match {
      case TypedLatte.Func(signature, code) => for {
        codeParsed <- mapM(code, instruction)
      } yield Latte.Func(transformSignature(signature), Latte.BlockInstruction(codeParsed))
    }
  }

  def exportConstructors(information: TypedLatte.CodeInformation): Compiler[List[Latte.Func]] = {
    val constructors: List[Latte.Func] = for {
      className <- information.containedClasses

      vtableAssignment = information.method(className).elts

      signature = Latte.FunctionSignature(
        className.constructor,
        VoidType,
        List(("this", PointerType(className))))

    } yield Latte.Func(signature, Latte.VtableFuncAssignment(vtableAssignment))

    ok(constructors)
  }


  override def compile(code: TypedLatte.Code): Either[CompileException, Latte.Code] = {
    val untyping = for {
      latteCode <- mapM(code._1.toList, compileFunc)
      constructors <- exportConstructors(code._2)
    } yield Latte.Code(constructors ::: latteCode, code._2.exportStructures, code._2)

    untyping.run(code._2)._2.toEither
  }
}
