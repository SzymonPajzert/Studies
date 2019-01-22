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

  class ArrayType(val eltType: Type) extends ClassType(s"array") {
    override def vtable: ClassType = ClassType("internal.empty")
  }

  case class ConstArrayType(eltType: Type, size: Int) extends Type {
    override def llvmRepr: String = s"[$size x ${eltType.llvmRepr}]"
  }

  case class ClassType(name: String) extends Type {
    override def llvmRepr: String = s"%class.$name"
    def vtable: ClassType = ClassType(s"$name.vtable")

    def vtableDefault: LLVM.Expression = LLVM.Value(s"@class.${vtable.name}.value", PointerType(vtable))

    def methodName(methodName: String): String = s"class.$name.method.$methodName"

    def constructor: String = s"class.$name.constructor"
  }

  case class AggregateType(toRef: ClassType, elements: Seq[Type]) extends Type {
    override def llvmRepr: String = s"%class.${toRef.name}"

    /**
      * Structure like "type { * }"
      * @return
      */
    def structure: String = {
      val eltsMapped = (elements map (_.llvmRepr)).mkString(",\n  ")
      s"type { \n  $eltsMapped \n}"
    }
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
