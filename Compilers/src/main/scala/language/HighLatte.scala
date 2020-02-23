package language

import language.Type.{PointerType, Type}

import scala.language.implicitConversions

object UntypedLatte extends HighLatte {
  override type CodeInformation = Unit
  override type ExpressionInformation = Unit

  implicit def namesAreVariables(identifier: String): LocationInf = (Variable(identifier), Unit)
  implicit def namesAreFunctionHandles(identifier: String): FunLocationInf = (FunName(identifier), Unit)

  def defaultValue(t: Type): UntypedLatte.ExpressionInf = t match {
    case Type.IntType => (UntypedLatte.ConstValue(0), Unit)
    case Type.PointerType(c: Type.ClassType) => (UntypedLatte.Null(PointerType(c)), Unit)
    case Type.StringType => (UntypedLatte.ConstValue(""), Unit)
    case c: Type.ClassType => (UntypedLatte.Null(PointerType(c)), Unit)
    case Type.VoidType => /* ignore */ (UntypedLatte.Null(PointerType(Type.VoidType)), Unit)
  }
}

abstract class LatteCompiler(val A: HighLatte, val B: HighLatte) {

  def mapInformation: A.ExpressionInformation => B.ExpressionInformation

  def locationInteresting: PartialFunction[A.LocationInf, B.LocationInf] = PartialFunction.empty
  def expressionInteresting: PartialFunction[A.ExpressionInf, B.ExpressionInf] = PartialFunction.empty
  def instructionInteresting: PartialFunction[A.Instruction, B.Instruction] = PartialFunction.empty

  def funLocation: A.FunLocationInf => B.FunLocationInf = {
    case (floc, inf) => (floc match {
      case x : A.FunName => B.FunName(x.name)
      case x : A.VTableLookup => B.VTableLookup(expression(x.expression), x.ident)
    }, mapInformation(inf))
  }


  def location: A.LocationInf => B.LocationInf = locationInteresting orElse {
    case (loc, inf) => (loc match {
      case A.Variable(ident) => B.Variable(ident)
      case A.ArrayAccess(arrayU, elementU) => B.ArrayAccess(
        expression(arrayU.asInstanceOf[A.ExpressionInf])  : B.ExpressionInf,
        expression(elementU.asInstanceOf[A.ExpressionInf]): B.ExpressionInf)
      case A.FieldAccess(placeU, elementU) => B.FieldAccess(
        expression(placeU.asInstanceOf[A.ExpressionInf]), elementU)
    }, mapInformation(inf))
  }

  def expression: A.ExpressionInf => B.ExpressionInf = expressionInteresting orElse {
    case (expr, inf) => (expr match {
      case x : A.FunctionCall =>
        B.FunctionCall(funLocation(x.location), x.arguments map expression)

      case A.ConstValue(value) => B.ConstValue(value)

      case x : A.ArrayCreation => B.ArrayCreation(x.typeT, expression(x.size))

      case x : A.Cast => B.Cast(x.t, expression(x.expression))

      case A.Null(c) => B.Null(c)

      case A.InstanceCreation(typeT) => B.InstanceCreation(typeT)

      case loc: A.Location => location((loc, inf))._1
    }, mapInformation(inf))
  }

  def instruction: A.Instruction => B.Instruction = instructionInteresting orElse {
    case x: A.Declaration => B.Declaration(x.identifier, x.typeValue)
    case x: A.Assignment => B.Assignment(location(x.location), expression(x.expr))
    case x: A.BlockInstruction => B.BlockInstruction(x.block map instruction)
    case x: A.DiscardValue => B.DiscardValue(expression(x.expression))
    case x: A.Return => B.Return(x.value map expression)
    case x: A.IfThen => B.IfThen(expression(x.condition), instruction(x.thenInst), x.elseOpt map instruction)
    case x: A.While => B.While(expression(x.condition), instruction(x.instr))
  }
}

object ParsedClasses extends HighLatte {
  override type CodeInformation = TypeInformation
  override type ExpressionInformation = Unit
}

object TypedLatte extends HighLatte {
  override type CodeInformation = TypeInformation
  override type ExpressionInformation = Type
}

trait HighLatte extends Language {
  self =>

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

  type Block = List[Instruction]

  sealed trait Instruction
  type ExpressionInf = (self.Expression, self.ExpressionInformation)
  type LocationInf = (self.Location, self.ExpressionInformation)
  type FunLocationInf = (self.FunLocation, self.ExpressionInformation)

  case class Declaration(identifier: String, typeValue: Type) extends Instruction with ClassMember
  case class Assignment(location: self.LocationInf, expr: self.ExpressionInf) extends Instruction
  case class BlockInstruction(block: Block) extends Instruction
  case class DiscardValue(expression: self.ExpressionInf) extends Instruction
  case class Return(value: Option[self.ExpressionInf]) extends Instruction
  case class IfThen(condition: self.ExpressionInf, thenInst: Instruction, elseOpt: Option[Instruction] = None) extends Instruction
  case class While(condition: self.ExpressionInf, instr: Instruction) extends Instruction

  /**
    * Specifies location of the functions and provides therefore access to methods
    */
  trait FunLocation {
    def pretty: String
  }
  case class FunName(name: String) extends FunLocation {
    def pretty: String = name
  }
  case class VTableLookup(expression: self.ExpressionInf, ident: String) extends FunLocation {
    def pretty: String = s"${expression._1.pretty}.$ident"
  }


  trait Expression {
    def isLiteral: Boolean = false
    def getType: Type = VoidType  // TODO remove
    def pretty: String
  }

  case class Null(nullType: PointerType) extends Expression {
    def pretty = "null"
  }
  case object Void extends Expression {
    def pretty = "void"
  }
  case class FunctionCall(location: self.FunLocationInf, arguments: Seq[self.ExpressionInf]) extends Expression {
    def pretty = s"${location._1.pretty}(${(arguments map (_._1.pretty)).mkString(", ")})"
  }
  case class ConstValue[+T](value: T) extends Expression {
    override def isLiteral: Boolean = true

    override def getType: Type = Unit match {
      case _ if value.isInstanceOf[Int] => IntType
      case _ if value.isInstanceOf[String] => StringType
      case _ if value.isInstanceOf[Boolean] => BoolType
      case _ => VoidType
    }

    def pretty: String = value.toString
  }
  case class Cast(t: Type, expression: self.ExpressionInf) extends Expression {
    def pretty = s"($t) ${expression._1.pretty}"
  }

  case class ArrayCreation(typeT: Type, size: self.ExpressionInf) extends Expression {
    def pretty = s"new array($typeT, ${size._1})"
  }

  case class InstanceCreation(typeT: Type) extends Expression {
    def pretty = s"new $typeT"
  }


  trait Location extends Expression
  case class Variable(identifier: String) extends Location {
    def pretty = identifier
  }
  case class ArrayAccess(array: self.ExpressionInf, element: self.ExpressionInf) extends Location {
    def pretty = s"${array._1.pretty}[${element._1.pretty}"
  }
  case class FieldAccess(place: self.ExpressionInf, element: String) extends Location {
    def pretty = s"${place._1.pretty}.${element}"
  }




  case class FunctionSignature(identifier: String, returnType: Type, arguments: List[(String, Type)]) {
    def getType: FunctionType = FunctionType(returnType, arguments map (_._2))
  }
}

