package compiler

import compiler.TypePhase.TypeEnvironment
import language.Latte.TypeInformation
import language.Type._
import language.{Type, TypedLatte, UntypedLatte}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

object TypePhase extends Compiler[UntypedLatte.Code, TypedLatte.Code] {
  type FunctionBinds = Map[String, Type]

  type S[A] = State[FunctionBinds, A]
  type TypeEnvironment[A] = EitherT[S, List[CompileException], A]

  implicit def viewableOnFirst[B, A <: B]
    (t: TypeEnvironment[(A, TypedLatte.ExpressionInformation)])
  : TypeEnvironment[(B, TypedLatte.ExpressionInformation)] = for {
    (value, information) <- t
  } yield (value, information)

  implicit def ok[A](value: A): TypeEnvironment[A] = EitherT[S, List[CompileException], A](state[FunctionBinds, List[CompileException] \/ A](\/-(value)))

  def createError[A](str: String): TypeEnvironment[A] = EitherT[S, List[CompileException], A](state(-\/(List(ErrorString(str)))))

  // LatteStaticAnalysis.mapM

  def mapM[A, B](list: List[A], f: A => TypeEnvironment[B]): TypeEnvironment[List[B]] =
    (list foldRight (ok(List()): TypeEnvironment[List[B]])) { (elt, acc) => for {
      newElt <- f(elt)
      okAcc <- acc
    } yield newElt :: okAcc
  }

  def location(loc: UntypedLatte.LocationInf): TypeEnvironment[TypedLatte.LocationInf] = loc._1 match {
    case UntypedLatte.Variable(identifier) => (TypedLatte.Variable(identifier), VoidType)

    case UntypedLatte.ArrayAccess(arrayU, elementU) => for {
      array <- expression(arrayU)
      element <- expression(elementU)
    } yield (TypedLatte.ArrayAccess(array, element), VoidType)

    case UntypedLatte.FieldAccess(placeU, element) => for {
      place <- expression(placeU)
    } yield (TypedLatte.FieldAccess(place, element), VoidType)
  }

  implicit def stateToEither[A](value: State[FunctionBinds, A]): TypeEnvironment[A] = {
    EitherT[S, List[CompileException], A](value map (\/-(_)))
  }



  def lookupFunctionSignature(functionName: String): TypeEnvironment[Type] = for {
    signatures <- get[FunctionBinds] : TypeEnvironment[FunctionBinds]

    foundValue <- (signatures.get(functionName) match {
      case Some(value) => ok(value)
      case None => createError(s"Function: $functionName not defined")
    }) : TypeEnvironment[Type]
  } yield foundValue

  def funLocation(fLoc: UntypedLatte.FunLocation): TypeEnvironment[TypedLatte.FunLocationInf] = fLoc match {
    case UntypedLatte.FunName(name: String) => for {
      functionType <- (name match {
        case "printInt" => FunctionType(VoidType, Seq(IntType))
        case "printString" => FunctionType (VoidType, Seq (StringType))
        case "int_add" => FunctionType (IntType, Seq (IntType, IntType))
        case "int_sub" => FunctionType (IntType, Seq (IntType, IntType))
        case "gen_gt" => FunctionType (BoolType, Seq (IntType, IntType))
        case "gen_lt" => FunctionType (BoolType, Seq (IntType, IntType))
        case userDefinedName => lookupFunctionSignature(userDefinedName)
      }) : TypeEnvironment[Type]
    } yield (TypedLatte.FunName(name), functionType)

    case UntypedLatte.VTableLookup(exprU, ident) => for {
      expr <- expression(exprU)
    } yield (TypedLatte.VTableLookup(expr, ident), VoidType)
  }

  def extractType(value: Any): Type = value match {
    case _ if value.isInstanceOf[Int] => Type.IntType
    case _ if value.isInstanceOf[Boolean] => Type.BoolType
    case _ if value.isInstanceOf[String] => Type.StringType
  }


  def returnType(functionName: String, t: Type): TypeEnvironment[Type] = t match {
    case FunctionType(rT, _) => rT
    case _ => createError(s"$functionName is not callable")
  }

  def expression(expr: UntypedLatte.ExpressionInf): TypeEnvironment[TypedLatte.ExpressionInf] = expr._1 match {
    case UntypedLatte.FunctionCall(locU , arguments) => for {
      loc <- funLocation(locU._1)
      args <- mapM(arguments.toList, expression)
      rt <- returnType(locU.toString, loc._2)

      // TODO check args match
    } yield (TypedLatte.FunctionCall(loc, args), rt)

    case UntypedLatte.ConstValue(value) => (TypedLatte.ConstValue(value), extractType(value))

    case UntypedLatte.ArrayCreation(elementType, size) => for {
      typedSize <- expression(size)
      result = (typedSize match {
        case (TypedLatte.ConstValue(v), Type.IntType) => {
          val arrayType = ArrayType(elementType, v.asInstanceOf[Int])
          (TypedLatte.ArrayCreation(elementType, typedSize), arrayType)
        }
      }) : TypedLatte.ExpressionInf
    } yield result

    case UntypedLatte.InstanceCreation(typeT) => (TypedLatte.InstanceCreation(typeT), typeT)
    case loc: UntypedLatte.Location => location((loc, Unit))
  }

  def instruction(ins: UntypedLatte.Instruction): TypeEnvironment[TypedLatte.Instruction] = ins match {
    case UntypedLatte.Declaration(identifier, typeValue) => TypedLatte.Declaration(identifier, typeValue)

    case UntypedLatte.Assignment(locU, exprU) => for{
      loc <- location(locU)
      expr <- expression(exprU)
    } yield TypedLatte.Assignment(loc, expr)

    case UntypedLatte.BlockInstruction(blockU) => for {
      block <- mapM(blockU, instruction)
    } yield TypedLatte.BlockInstruction(block)

    case UntypedLatte.DiscardValue(expr) => expression(expr) map TypedLatte.DiscardValue
    case UntypedLatte.Return(value) => value match {
      case None => TypedLatte.Return(None)
      case Some(v) => expression(v) map (vE => TypedLatte.Return(Some(vE)))
    }

    case UntypedLatte.IfThen(conditionU, thenInstU, elseUOpt) => for {
      condition <- expression(conditionU)
      thenInst <- instruction(thenInstU)
      elseOpt <- (elseUOpt match {
        case None => None
        case Some(els) => instruction(els) map (Some(_))
      }): TypeEnvironment[Option[TypedLatte.Instruction]]
    } yield TypedLatte.IfThen(condition, thenInst, elseOpt)

    case UntypedLatte.While(conditionU, instrU) => for {
      condition <- expression(conditionU)
      instr <- instruction(instrU)
    } yield TypedLatte.While(condition, instr)
  }

  def toplevelFunc: UntypedLatte.Func => TypeEnvironment[TypedLatte.Func] = {
    case UntypedLatte.Func(signature, codeU) => for {
      code <- mapM(codeU, instruction)
    } yield TypedLatte.Func(
        signature.asInstanceOf[TypedLatte.FunctionSignature], code)
  }

  def member: UntypedLatte.ClassMember => TypeEnvironment[TypedLatte.ClassMember] = {
    case func : UntypedLatte.Func => for {
      f <- toplevelFunc(func)
    } yield f
    case UntypedLatte.Declaration(identifier, typeValue) =>
      TypedLatte.Declaration(identifier, typeValue)
  }

  def topDefinition: UntypedLatte.TopDefinition => TypeEnvironment[TypedLatte.TopDefinition] = {
    case func : UntypedLatte.Func => for {
      f <- toplevelFunc(func)
    } yield f
    case UntypedLatte.Class(name, base, insidesU) => for {
      insides <- mapM(insidesU, member)
    } yield TypedLatte.Class(name, base, insides)
  }

  def addTopDefinition: UntypedLatte.TopDefinition => TypeEnvironment[Unit] = {
    case UntypedLatte.Func(signature, _) => (for {
      currentBinds <- get[FunctionBinds]: TypeEnvironment[FunctionBinds]
      name = signature.identifier

      _ <- (currentBinds get name match {
        case Some(_) => createError(s"$name already defined")
        case None => modify[FunctionBinds]{
          _ + (name -> FunctionType(signature.returnType, signature.arguments.map(_._2)))
        }: TypeEnvironment[Unit]
      }): TypeEnvironment[Unit]
    } yield Unit): TypeEnvironment[Unit]

    case _ => ok(Unit)
  }

  override def compile(code: UntypedLatte.Code): Either[List[CompileException], TypedLatte.Code] = {
    val typePhase: TypeEnvironment[TypedLatte.Code] = for {
      _ <- mapM(code._1.toList, addTopDefinition)
      typedCode <- mapM(code._1.toList, topDefinition)
    } yield (typedCode, new TypeInformation)

    typePhase.run(Map())._2.toEither
  }
}
