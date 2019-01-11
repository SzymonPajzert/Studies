package language

import scala.language.implicitConversions

object Latte extends Language {
  import language.Type._
  LanguageRegister.register(Latte)



  case class Code(definitions: Seq[Func],
                  globalLLVM: String = "",
                  typeInformation: TypeInformation) {
    def signatures: Map[String, FunctionType] = {
      (definitions map (func => {
        func.signature.identifier -> FunctionType(func.signature.returnType, func.signature.arguments map (_._2))
      })).toMap
    }
  }

  trait Block
  case class VtableFuncAssignment(funcs: List[(String, FunctionType)]) extends Block

  trait Expression {
    def isLiteral: Boolean = false
    def getType: Type = VoidType
  }
  case class Null(t: Type) extends Expression
  case object Void extends Expression

  trait FunLocation
  case class FunName(name: String) extends FunLocation
  case class VTableLookup(expression: Expression, offset: Int, funcType: FunctionType) extends FunLocation

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
  case class BlockInstruction(block: List[Instruction]) extends Instruction with Block
  case class DiscardValue(expression: Expression) extends Instruction
  case class Return(value: Option[Expression]) extends Instruction
  case class IfThen(condition: Expression, thenInst: Instruction, elseOpt: Option[Instruction] = None) extends Instruction
  case class While(condition: Expression, instr: Instruction) extends Instruction
}

