// reviewed: 2018.12.28

package compiler

import language.Type._
import language.{Latte, TypedLatte}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

object UntypingPhase extends Compiler[TypedLatte.Code, Latte.Code] {
  type KS = TypedLatte.CodeInformation
  type S[A] = State[KS, A]
  type Compiler[E] = EitherT[S, List[CompileException], E]

  implicit def ok[A](value: A): Compiler[A] = EitherT[S, List[CompileException], A](state[KS, List[CompileException] \/ A](\/-(value)))

  implicit def stateToEither[A](value: State[KS, A]): Compiler[A] = {
    EitherT[S, List[CompileException], A](value map (\/-(_)))
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
        offset = codeInformation.fieldOffset(className).offset(element).get
        expression <- compileExpr(expressionInf)
      } yield Latte.FieldAccess(expression, offset)): Compiler[Latte.Location]
  }

  def transformType(addPtr: Boolean): Type => Compiler[Type] = {
    case ClassType(className) => for {
      codeInformation <- get[TypedLatte.CodeInformation]: Compiler[TypedLatte.CodeInformation]

      types = codeInformation.fieldTypes(ClassType(className)) map {
        case c : ClassType => PointerType(c)
        case t => t
      }

      vtableId = s"$className.vtable"
      result = AggregateType(className, PointerType(AggregateType(vtableId, Seq())) :: types.toList) : Type
    } yield if(addPtr) PointerType(result) else result
    case PointerType(t) => transformType(false)(t) map PointerType
    case a => a
  }

  def compileExpr(expression: TypedLatte.ExpressionInf): Compiler[Latte.Expression] = {
    expression._1 match {
      case TypedLatte.FunctionCall((TypedLatte.FunName(functionName), _), a) => for {
        arguments <- mapM(a.toList, compileExpr)
      } yield Latte.FunctionCall(Latte.FunName(functionName), arguments)

      case TypedLatte.FunctionCall((TypedLatte.VTableLookup(exprInfU, ident), _), argumentsU) => for {
        expr <- compileExpr(exprInfU)
        typeInformation <- get[KS]: Compiler[KS]
        offset = typeInformation.methodOffset(exprInfU._2.asInstanceOf[ClassType]).offset(ident).get
        arguments <- mapM(argumentsU.toList, compileExpr)
      } yield Latte.FunctionCall(Latte.VTableLookup(expr, offset), arguments)

      case TypedLatte.ConstValue(v) => Latte.ConstValue(v)

      case TypedLatte.ArrayCreation(a, b) => compileExpr(b) map (Latte.ArrayCreation(a, _))

      case TypedLatte.InstanceCreation(classType: ClassType) =>
        transformType(false)(classType) map Latte.InstanceCreation

      case locU: TypedLatte.Location => for {
        loc <- location(locU)
      } yield loc

      case TypedLatte.Null => Latte.Null(expression._2)

      case TypedLatte.Void => Latte.Void
    }
  }

  def instruction: TypedLatte.Instruction => Compiler[Latte.Instruction] = {
    case TypedLatte.Declaration(name, typeDecl) =>
      transformType(false)(typeDecl) map {
        case t: AggregateType => Latte.Declaration(name, PointerType(t))
        case t => Latte.Declaration(name, t)
      }

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
      className <- information.defined.keys.toList
      vtableAssignment = information.methodOffset(className).elts
      signature = Latte.FunctionSignature(className.constructor, VoidType, List(("this", PointerType(className))))
    } yield Latte.Func(signature, Latte.VtableFuncAssignment(vtableAssignment))

    ok(constructors)
  }

  def exportStructures(information: Latte.TypeInformation): Compiler[String] = ok((for {
    className <- information.containedClasses
    elements = information.fieldTypes(className).map(_.llvmRepr).mkString(", ")

    methods = information.methodTypes(className).map("  " + PointerType(_).llvmRepr).mkString(",\n")

    vtableType = PointerType(className.vtable).llvmRepr
    vtable = s"${className.vtable.llvmRepr} = type { \n$methods\n}\n"
    classType = s"${className.llvmRepr} = type { $vtableType, $elements }"
  } yield s"$vtable\n$classType").mkString("\n\n"))


  override def compile(code: TypedLatte.Code): Either[List[CompileException], Latte.Code] = {
    val untyping = for {
      latteCode <- mapM(code._1.toList, compileFunc)
      constructors <- exportConstructors(code._2)
      allStructures <- exportStructures(code._2)
    } yield Latte.Code(constructors ::: latteCode, allStructures, code._2)

    untyping.run(code._2)._2.toEither
  }
}
