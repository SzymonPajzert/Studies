package language

object Type {
  case class DerefException(t: Type) extends Exception(s"trying to deref: $t")

  sealed trait Type {
    def deref: Type = throw new DerefException(this)
    def llvmRepr: String
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
  }

  case class AggregateType(name: String, elements: Seq[Type]) extends Type {
    override def llvmRepr: String = s"%class.$name"
  }

  case object StringType extends Type {
    override def llvmRepr: String = ???
  }

  case object BoolType extends Type {
    override def llvmRepr: String = ???
  }

  case class PointerType(t: Type) extends Type {
    override def llvmRepr: String = s"${t.llvmRepr}*"

    override def deref: Type = t
  }

  case class FunctionType(returnType: Type, argsType: Seq[Type]) extends Type {
    override def llvmRepr: String = ???
  }
}
