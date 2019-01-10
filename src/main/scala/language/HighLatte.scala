package language

import language.Latte.TypeInformation
import language.Type.Type

import scala.language.implicitConversions

object UntypedLatte extends HighLatte {
  override type CodeInformation = Unit
  override type ExpressionInformation = Unit

  implicit def namesAreVariables(identifier: String): LocationInf = (Variable(identifier), Unit)
  implicit def namesAreFunctionHandles(identifier: String): FunLocationInf = (FunName(identifier), Unit)

  def defaultValue(t: Type): UntypedLatte.ExpressionInf = t match {
    case Type.IntType => (UntypedLatte.ConstValue(0), Unit)
    case Type.PointerType(c: Type.ClassType) => (UntypedLatte.Null(c), Unit)
    case Type.StringType => (UntypedLatte.ConstValue(""), Unit)
    case c: Type.ClassType => (UntypedLatte.Null(c), Unit)
  }
}

object TypedLatte extends HighLatte {
  override type CodeInformation = TypeInformation
  override type ExpressionInformation = Type
}

trait HighLatte extends Language {
  import language.Type._
  LanguageRegister.register(Latte)

  type CodeInformation
  type ExpressionInformation

  type Code = (Seq[TopDefinition], CodeInformation)

  def findFunction(code: Code, name: String): Option[Func] = {
    code._1 find {
      case Func(signature, _) if signature.identifier == name => true
      case _ => false
    }
  }.asInstanceOf[Option[Func]]

  def findAssignment(func: Func, name: String): Option[ExpressionInf] = func.code collectFirst {
    case Assignment((Variable(nameM), _), expr) if nameM == name => expr
  }

  trait TopDefinition
  case class Func(signature: FunctionSignature, code: Block) extends TopDefinition with ClassMember
  case class Class(name: String, base: String, insides: List[ClassMember]) extends TopDefinition

  trait ClassMember

  def mapFunLocOnLocation(locF: Location => Location): FunLocationInf => FunLocationInf = {
    case ((VTableLookup(expression, ident), i)) =>
      (VTableLookup(mapExpressionOnLocation(locF)(expression), ident), i)
    case t => t
  }

  type Block = List[Instruction]
  def mapExpressionOnLocation(locF: Location => Location): ExpressionInf => ExpressionInf =
    {
      case (FunctionCall(location, arguments), i) => (FunctionCall(
        mapFunLocOnLocation(locF)(location), arguments map mapExpressionOnLocation(locF)), i)

      case (Cast(typeT, expression), i) => (Cast(typeT, mapExpressionOnLocation(locF)(expression)), i)
      case (ArrayCreation(typeT, size), i) => (ArrayCreation(typeT, mapExpressionOnLocation(locF)(size)), i)
      case (loc: Location, i) => (locF(loc), i)
      case a => a
    }

  def mapBlockOnLocation(locF: Location => Location): Block => Block = { block =>
    def e: ExpressionInf => ExpressionInf = mapExpressionOnLocation(locF)

    def i: Instruction => Instruction = {
      case Assignment(loc, expr) => Assignment((locF(loc._1), loc._2), e(expr))
      case BlockInstruction(b) => BlockInstruction(b map i)
      case DiscardValue(expression) => DiscardValue(e(expression))
      case Return(value: Option[ExpressionInf]) => Return(value map e)
      case IfThen(condition, thenInst, elseOpt) => IfThen(e(condition), i(thenInst), elseOpt map i)
      case While(condition, instr) => While(e(condition), i(instr))
      case any => any
    }

    block map i
  }

  sealed trait Instruction
  type ExpressionInf = (Expression, ExpressionInformation)
  type LocationInf = (Location, ExpressionInformation)
  type FunLocationInf = (FunLocation, ExpressionInformation)

  case class Declaration(identifier: String, typeValue: Type) extends Instruction with ClassMember
  case class Assignment(location: LocationInf, expr: ExpressionInf) extends Instruction
  case class BlockInstruction(block: Block) extends Instruction
  case class DiscardValue(expression: ExpressionInf) extends Instruction
  case class Return(value: Option[ExpressionInf]) extends Instruction
  case class IfThen(condition: ExpressionInf, thenInst: Instruction, elseOpt: Option[Instruction] = None) extends Instruction
  case class While(condition: ExpressionInf, instr: Instruction) extends Instruction

  /**
    * Specifies location of the functions and provides therefore access to methods
    */
  trait FunLocation
  case class FunName(name: String) extends FunLocation
  case class VTableLookup(expression: ExpressionInf, ident: String) extends FunLocation


  trait Expression {
    def isLiteral: Boolean = false
    def getType: Type = VoidType  // TODO remove
  }

  case class Null(classType: ClassType) extends Expression
  case object Void extends Expression
  case class FunctionCall(location: FunLocationInf, arguments: Seq[ExpressionInf]) extends Expression
  case class ConstValue[+T](value: T) extends Expression {
    override def isLiteral: Boolean = true

    override def getType: Type = Unit match {
      case _ if value.isInstanceOf[Int] => IntType
      case _ if value.isInstanceOf[String] => StringType
      case _ if value.isInstanceOf[Boolean] => BoolType
      case _ => VoidType
    }
  }
  case class Cast(t: Type, expression: ExpressionInf) extends Expression

  case class ArrayCreation(typeT: Type, size: ExpressionInf) extends Expression
  case class InstanceCreation(typeT: Type) extends Expression


  trait Location extends Expression
  case class Variable(identifier: String) extends Location
  case class ArrayAccess(array: ExpressionInf, element: ExpressionInf) extends Location
  case class FieldAccess(place: ExpressionInf, element: String) extends Location




  case class FunctionSignature(identifier: String, returnType: Type, arguments: List[(String, Type)]) {
    def getType: FunctionType = FunctionType(returnType, arguments map (_._2))
  }
}

