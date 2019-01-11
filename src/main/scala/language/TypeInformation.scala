package language

import language.Type._

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

object TypeInformation {
  def builder: TypeInformationBuilder = new TypeInformationBuilder(Map(), Map(), Map())
  def empty: TypeInformation = new TypeInformation(Map(), Map())
}

class TypeInformationBuilder(private val subclasses: Map[ClassType, Set[ClassType]],
                             private val parent: Map[ClassType, Option[ClassType]],
                             private val defined: Map[ClassType, Offset]) {
  def addClassStructure(classT: ClassType, base: Option[ClassType],
                        fields: Seq[(String, Type)],
                        methods: Seq[(String, FunctionType)]): TypeInformationBuilder = {
    val methodContainer = OffsetContainer(methods.toList)

    new TypeInformationBuilder(subclasses, parent,
      defined + (classT -> Offset(
        OffsetContainer(fields.toList), methodContainer
      )))
  }

  def build: TypeInformation = TypeInformation(parent, defined)
}

case class TypeInformation(private val parent: Map[ClassType, Option[ClassType]],
                           private val defined: Map[ClassType, Offset]) {
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

  /**
    * Return if t can be used as v
    * @param t
    * @param v
    */
  def viewable(t: Type, v: Type): Boolean = false


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
