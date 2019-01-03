package language

import scala.language.implicitConversions

object Latte extends Language {
  import language.Type._
  LanguageRegister.register(Latte)

  class TypeInformation {
    def offsetForClass(className: Type): FieldOffset = ???
  }

  class FieldOffset {
    def fieldOffset(field: String): Option[Int] = None
    def methodOffset(method: String): Option[Int] = None
  }

  case class Code(definitions: Seq[Func])

  type Block = List[Instruction]

  trait Expression {
    def isLiteral: Boolean = false
    def getType: Type = VoidType
  }
  trait FunLocation
  case class FunName(name: String) extends FunLocation
  case class VTableLookup(expression: Expression, offset: Int) extends FunLocation

  case class FunctionCall(location: FunLocation, arguments: Seq[Expression]) extends Expression
  case class ConstValue[+T](value: T) extends Expression {
    override def isLiteral: Boolean = true

    override def getType: Type = Unit match {
      case _ if value.isInstanceOf[Int] => IntType
      case _ if value.isInstanceOf[String] => StringType
      case _ if value.isInstanceOf[Boolean] => BoolType
      case _ => VoidType
    }
  }
  case class ArrayAccess(array: Expression, element: Expression) extends Expression with Location
  case class FieldAccess(place: Expression, element: Int) extends Expression with Location
  case class Variable(identifier: String) extends Expression with Location

  case class ArrayCreation(typeT: Type, size: Expression) extends Expression
  case class InstanceCreation(typeT: Type) extends Expression

  trait Location
  implicit def namesAreVariables(identifier: String): Variable = Variable(identifier)

  case class Func(signature: FunctionSignature, code: Block)

  case class FunctionSignature(identifier: String, returnType: Type, arguments: List[(String, Type)])

  trait Instruction
  case class Declaration(identifier: String, typeValue: Type) extends Instruction
  case class Assignment(location: Location, expr: Expression) extends Instruction
  case class BlockInstruction(block: Block) extends Instruction
  case class DiscardValue(expression: Expression) extends Instruction
  case class Return(value: Option[Expression]) extends Instruction
  case class IfThen(condition: Expression, thenInst: Instruction, elseOpt: Option[Instruction] = None) extends Instruction
  case class While(condition: Expression, instr: Instruction) extends Instruction
}

