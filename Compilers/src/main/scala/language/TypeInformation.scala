package language

import compiler.TypePhase.ok
import language.Type._

object Offset {
  def empty: Offset = Offset(OffsetContainer(List()), OffsetContainer(List()))
}

case class Offset(fields: OffsetContainer[Type, Any], methods: OffsetContainer[FunctionType, ClassType]) {
  def addField[A,B,C](list: Seq[(A, B)], value: C): List[(A, B, C)] = for {
    (x, y) <- list.toList
  } yield (x, y, value)

  def extendBy(extendingClass: ClassType,
               newFields: Seq[(String, Type)],
               newMethods: Seq[(String, FunctionType)]): Offset = {
    val existingFields = fields.elts.map(_._1).toSet
    val existingMethods = methods.elts.map(_._1).toSet
    val newMethodsSet = newMethods.map(_._1).toSet

    // Change type of self argument
    val oldMethods = methods.elts map { case (name, signature, defining) => if (newMethodsSet contains name)
      (name, signature.copy(argsType = PointerType(extendingClass) :: signature.argsType.tail.toList), extendingClass)
      else (name, signature, defining)
    }

    val newFieldsMapped = addField(newFields, Unit) filter (x => !(existingFields contains x._1))
    val newMethodsMapped = addField(newMethods, extendingClass) filter (x => !(existingMethods contains x._1))

    Offset(
      OffsetContainer[Type, Any](fields.elts ::: newFieldsMapped),
      OffsetContainer(oldMethods ::: newMethodsMapped)
    )
  }
}

case class OffsetContainer[T <: Type, E](elts: List[(String, T, E)]) {
  def find(field: String): Option[(T, E)] = elts.find(_._1 == field).map(x => (x._2, x._3))

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

case class TypeInformationBuilder(private val parent: Map[ClassType, ClassType],
                                  private val defined: Map[ClassType, Offset],
                                  private val modifiers: Map[ClassType, TypeInformationBuilder => TypeInformationBuilder]) {
  def addClassStructure(classT: ClassType, base: Option[ClassType],
                        fields: Seq[(String, Type)],
                        methods: Seq[(String, FunctionType)]): TypeInformationBuilder = {

    // In this case we need to wait for the data
    if (base.nonEmpty && defined.get(base.get).isEmpty) {
      val currentModifier = modifiers.getOrElse(classT, (x: TypeInformationBuilder) => x)
      val nextModifier = currentModifier.andThen(_.addClassStructure(classT, base, fields, methods))
      this.copy(modifiers = modifiers + (base.get -> nextModifier))
    } else {

      // Here we know that all the necessary data has been already passed
      val offset = base match {
        case Some(baseClass) => defined(baseClass).extendBy(classT, fields, methods)
        case None => Offset.empty.extendBy(classT, fields, methods)
      }
      val classAccepted = this.copy(defined = defined + (classT -> offset))
      val parentAdded = base match {
        case None => classAccepted
        case Some(b) => classAccepted.copy(parent = parent + (classT -> b))
      }

      val modifier = this.modifiers.getOrElse(classT, (x: TypeInformationBuilder) => x)
      modifier(parentAdded.copy(modifiers = modifiers - classT))
    }
  }

  def build: TypeInformation = {
    assert(modifiers.isEmpty)
    TypeInformation(parent, defined)
  }
}


object TypeInformation {
  def builder: TypeInformationBuilder = TypeInformationBuilder(Map(), Map(), Map())
  def empty: TypeInformation = TypeInformation(Map(), Map())
}


case class TypeInformation(private val parents: Map[ClassType, ClassType],
                           private val defined: Map[ClassType, Offset]) {
  def isParent(subclass: Type, baseclass: Type): Boolean =  (subclass, baseclass) match {
    case (PointerType(a: ClassType), PointerType(b: ClassType)) =>
      parents.get(a) match {
        case None => false
        case Some(parent) => (parent == b) || isParent(PointerType(parent), baseclass)
      }

    case (null, ptr: PointerType) => true

    case _ => false
  }

  def contains(className: ClassType): Boolean = {
    className match {
      case _: ArrayType => true
      case _ => defined contains className
    }
  }

  private def lookup(className: ClassType): Offset = {
    className match {
      case _: ArrayType =>
        Offset(
          fields = OffsetContainer(List(
            ("data", PointerType(CharType), Unit),
            ("length", IntType, Unit))),
          methods = OffsetContainer(List()))
      case _ => defined(className)
    }
  }

  def field(className: ClassType): OffsetContainer[Type, Any] = lookup(className).fields
  def method(className: ClassType): OffsetContainer[FunctionType, ClassType] = lookup(className).methods

  def vtable(className: ClassType): AggregateType = AggregateType(
    className.vtable,
    method(className).types.map(PointerType))

  def aggregate(className: ClassType): AggregateType = {
    val vtableType = PointerType(vtable(className).toRef)
    val fields = field(className).types.toList
    AggregateType(className, vtableType :: fields)
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
    className <- new ArrayType(VoidType) :: containedClasses

    vtable = s"${className.vtable.llvmRepr} = ${this.vtable(className).structure}"

    funcIdentifiers = method(className).elts.map { case ((name, fType, definedInClass)) =>
      s"${PointerType(fType).llvmRepr} @${definedInClass.methodName(name)}"
    }

    defaultVtable =
    s"""${className.vtableDefault.name} = global ${className.vtable.llvmRepr} {
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
