package compiler

import language.Type._
import language.{Type => _, _}

import scala.language.implicitConversions
import scalaz.Scalaz._
import scalaz._

object IdTransformation extends LatteCompiler(UntypedLatte, ParsedClasses) {
  def mapInformation: A.ExpressionInformation => B.ExpressionInformation =
    (_) => Unit.asInstanceOf[B.ExpressionInformation]
}

class MethodTransformation(val fields: Map[String, Type]) extends LatteCompiler(UntypedLatte, ParsedClasses) {
  def mapInformation: A.ExpressionInformation => B.ExpressionInformation =
    (_) => Unit.asInstanceOf[B.ExpressionInformation]

  override def locationInteresting: PartialFunction[A.LocationInf, B.LocationInf] = {
    case (A.Variable(identifier), inf) if fields contains identifier =>
      (B.FieldAccess((B.Variable("self"), ().asInstanceOf[B.ExpressionInformation]), identifier),
        mapInformation(inf))
  }
}


/**
  * Performs following checks:
  *   - static binding of variables
  *   - renaming hiding variables
  *   - typing the expressions
  */
object ParseClasses extends Compiler[UntypedLatte.Code, ParsedClasses.Code] {

  type KS = TypeInformationBuilder
  type S[A] = State[KS, A]
  type TypeEnvironment[A] = EitherT[S, CompileException, A]



  implicit def ok[A](value: A): TypeEnvironment[A] =
    EitherT[S, CompileException, A](state[KS, CompileException \/ A](\/-(value)))

  implicit def stateToEither[A](value: State[KS, A]): TypeEnvironment[A] = {
    EitherT[S, CompileException, A](value map (\/-(_)))
  }

  def mapM[A, B](list: List[A], f: A => TypeEnvironment[B]): TypeEnvironment[List[B]] = list match {
    case Nil => Nil
    case (hM :: tM) => for {
      h <- f(hM)
      t <- mapM(tM, f)
    } yield h :: t
  }


  def convertMemberFunction(identifierTypePairs: List[(String, Type)],
                            classType: ClassType): UntypedLatte.ClassMember => Seq[(String, ParsedClasses.Func)] = {
    case f: UntypedLatte.Func =>
      val sig = f.signature

      val newSignature = ParsedClasses.FunctionSignature(
        returnType = sig.returnType,
        arguments = ("self", PointerType(classType)) :: sig.arguments,
        identifier = classType.methodName(sig.identifier))

      val fields: Map[String, Type] = identifierTypePairs.toMap

      val newCode = f.code map
        new MethodTransformation(fields).instruction.asInstanceOf[UntypedLatte.Instruction => ParsedClasses.Instruction]

      val newFunction = ParsedClasses.Func(newSignature, newCode)

      Seq((sig.identifier, newFunction))
    case UntypedLatte.Declaration(_, _) => Seq()
  }

  def parseClassStructure(classT: ClassType, base: Option[ClassType],
                          members: List[UntypedLatte.ClassMember]): TypeEnvironment[List[ParsedClasses.Func]] = {
    val identifierTypePairs = members flatMap {
      case UntypedLatte.Declaration(value, c : ClassType) => Seq((value, PointerType(c)))
      case UntypedLatte.Declaration(value, t) => Seq((value, t))
      case _ => Seq()
    }

    val memberFunctions: Seq[(String, ParsedClasses.Func)] =
      members flatMap convertMemberFunction(identifierTypePairs, classT)

    val memberFunctionsPairs = memberFunctions map {
      case (identifier, func) => (identifier, func.signature.getType)
    }

    for {
      _ <- modify[KS] {
        _.addClassStructure(classT, base, identifierTypePairs, memberFunctionsPairs)
      } : TypeEnvironment[Unit]
    } yield memberFunctions.map(_._2).toList
  }

  /**
    * Parse top level definitions and return methods that were hidden inside classes
    * along existing functions
    * @return
    */
  def addTopDefinition: UntypedLatte.TopDefinition => TypeEnvironment[List[ParsedClasses.Func]] = {
    case f: UntypedLatte.Func => {
      val sig = f.signature

      val newSignature = ParsedClasses.FunctionSignature(
        sig.identifier,
        sig.returnType, sig.arguments)

      val newCode = f.code map IdTransformation.instruction.asInstanceOf[UntypedLatte.Instruction => ParsedClasses.Instruction]

      List(ParsedClasses.Func(newSignature, newCode))
    }

    case UntypedLatte.Class(name, base, insidesU) => for {
      methods <- parseClassStructure(
        ClassType(name),
        if(base == "Object") None else Some(ClassType(base)),
        insidesU)
    } yield methods
  }

  def emptyState: KS = TypeInformation.builder

  override def compile(code: UntypedLatte.Code): Either[CompileException, ParsedClasses.Code] = {
    val typePhase: TypeEnvironment[ParsedClasses.Code] = for {
      methodsAndFunctions <- mapM(code._1.toList, addTopDefinition)
      typeInformationBuilder <- get[KS]: TypeEnvironment[KS]
    } yield (methodsAndFunctions.flatten, typeInformationBuilder.build)

    typePhase.run(emptyState)._2.toEither
  }
}
