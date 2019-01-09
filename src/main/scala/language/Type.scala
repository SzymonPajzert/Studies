package language

object Type {
  case class DerefException(t: Type) extends Exception(s"trying to deref: $t")

  sealed trait Type {
    def deref: Type = throw DerefException(this)
    def llvmRepr: String
    def ptr: PointerType = PointerType(this)
  }

  case object IntType extends Type {
    override def llvmRepr: String = "i32"
  }
  case object VoidType extends Type {
    override def llvmRepr: String = "void"
  }
  case object CharType extends Type {
    override def llvmRepr: String = "i8"
  }
  case class ArrayType(eltType: Type) extends Type {
    override def llvmRepr: String = ???
  }

  case class ConstArrayType(eltType: Type, size: Int) extends Type {
    override def llvmRepr: String = s"[$size x ${eltType.llvmRepr}]"
  }

  case class ClassType(name: String) extends Type {
    override def llvmRepr: String = s"%class.$name"
    def vtable: ClassType = ClassType(s"$name.vtable")

    def vtableDefault: String = s"@class.${vtable.name}.value"

    def methodName(methodName: String): String = s"class.$name.method.$methodName"

    def constructor: String = s"class.$name.constructor"
  }

  case class AggregateType(name: String, elements: Seq[Type]) extends Type {
    override def llvmRepr: String = s"%class.$name"

    /**
      * Structure like "type { * }"
      * @return
      */
    def structure: String = {
      val eltsMapped = (elements map (_.llvmRepr)).mkString(",\n  ")
      s"type { \n  $eltsMapped \n}"
    }

    def toRef: ClassType = ClassType(name)
  }

  case object StringType extends Type {
    override def llvmRepr: String = ???
  }

  case object BoolType extends Type {
    override def llvmRepr: String = "i1"
  }

  case class PointerType(t: Type) extends Type {
    override def llvmRepr: String = s"${t.llvmRepr}*"

    override def deref: Type = t
  }

  case class FunctionType(returnType: Type, argsType: Seq[Type]) extends Type {
    override def llvmRepr: String = {
      val args = (argsType map (_.llvmRepr)).mkString(",")
      s"${returnType.llvmRepr}($args)"
    }
  }
}
