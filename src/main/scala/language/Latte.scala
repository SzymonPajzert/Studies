package language

import scala.language.implicitConversions

object Latte extends Language {
  import language.Type._
  LanguageRegister.register(Latte)

  object TypeInformation {
    def empty: TypeInformation = new TypeInformation(Map())
  }

  case class TypeInformation(defined: Map[ClassType, Offset]) {
    def fieldOffset(className: ClassType): OffsetContainer = defined(className).fields
    def methodOffset(className: ClassType): OffsetContainer = defined(className).methods

    def fieldType(className: ClassType, field: String): Option[Type] =
      fieldOffset(className).elts.find(_._1 == field).map(_._2)
    def methodType(className: ClassType, field: String): Option[Type] =
      methodOffset(className).elts.find(_._1 == field).map(_._2)

    def fieldTypes(className: ClassType): Seq[Type] = fieldOffset(className).types

    def containedClasses: List[ClassType] = defined.keys.toList
  }

  case class Offset(fields: OffsetContainer, methods: OffsetContainer)

  case class OffsetContainer(elts: List[(String, Type)]) {
    def types: Seq[Type] = elts map (_._2)

    def offset(elt: String): Option[Int] =
      elts
        .map(_._1)
        .zipWithIndex
        .find(_._1 == elt)
        .map (_._2)
  }

  case class Code(definitions: Seq[Func],
                  globalLLVM: String = "",
                  typeInformation: TypeInformation)

  type Block = List[Instruction]

  trait Expression {
    def isLiteral: Boolean = false
    def getType: Type = VoidType
  }
  case object Void extends Expression
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
  case class ArrayAccess(array: Expression, element: Expression) extends Location
  case class FieldAccess(place: Expression, element: Int) extends Location
  case class Variable(identifier: String) extends Location

  case class ArrayCreation(typeT: Type, size: Expression) extends Expression
  case class InstanceCreation(typeT: Type) extends Expression

  trait Location extends Expression
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

