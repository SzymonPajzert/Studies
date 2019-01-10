package language

import scala.language.implicitConversions

object Latte extends Language {
  import language.Type._
  LanguageRegister.register(Latte)

  object TypeInformation {
    def empty: TypeInformation = new TypeInformation(Map())
  }

  case class TypeInformation(private val defined: Map[ClassType, Offset]) {
    def field(className: ClassType): OffsetContainer[Type] = defined(className).fields
    def method(className: ClassType): OffsetContainer[FunctionType] = defined(className).methods

    def vtable(className: ClassType): AggregateType = AggregateType(
      s"${className.name}.vtable",
      method(className).types.map(PointerType))

    def aggregate(className: ClassType): AggregateType = {
      val vtableType = PointerType(vtable(className).toRef)
      val fields = field(className).types.toList
      AggregateType(className.name, vtableType :: fields)
    }

    def containedClasses: List[ClassType] = defined.keys.toList

    def addClassStructure(classT: ClassType,
                          fields: Seq[(String, Type)],
                          methods: Seq[(String, FunctionType)]): TypeInformation = {


      new TypeInformation(defined + (classT -> Offset(
        OffsetContainer(fields.toList), OffsetContainer(methods.toList)
      )))
    }

    def memsize(classType: ClassType): Int = {
      aggregate(classType).elements map {
        case PointerType(_) => 8
        case IntType => 4
        case _ => 4
      } sum
    }


    def exportStructures: String = (for {
      className <- containedClasses

      vtable = s"${className.vtable.llvmRepr} = ${this.vtable(className).structure}"

      funcIdentifiers = method(className).elts.map { case ((name, fType)) =>
        s"${PointerType(fType).llvmRepr} @${className.methodName(name)}"
      }

      defaultVtable =
        s"""${className.vtableDefault} = global ${className.vtable.llvmRepr} {
           |  ${funcIdentifiers.mkString(",\n  ")}
           |}""".stripMargin

      classType = s"${className.llvmRepr} = ${this.aggregate(className).structure}"
    } yield
      s"""
         |$vtable
         |
         |$defaultVtable
         |
         |$classType""".stripMargin).mkString("\n\n\n")
  }

  case class Offset(fields: OffsetContainer[Type], methods: OffsetContainer[FunctionType])

  case class OffsetContainer[T <: Type](elts: List[(String, T)]) {
    def types: Seq[T] = elts map (_._2)

    def findType(field: String): Option[T] =
      elts.find(_._1 == field).map(_._2)

    def offset(elt: String): Option[Int] =
      elts
        .map(_._1)
        .zipWithIndex
        .find(_._1 == elt)
        .map (_._2)
  }

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

