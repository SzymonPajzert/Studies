package language

object Type {
  sealed trait Type {
    def deref: Type = throw new Exception(s"trying to deref: ${this}")
  }
  sealed trait LLVMType extends Type

  sealed trait ValueType extends Type with LLVMType

  case object IntType extends ValueType {
    override def toString: String = "i32"
  }
  case object VoidType extends ValueType {
    override def toString: String = "void"
  }
  case object CharType extends ValueType {
    override def toString: String = "i8"
  }

  case object StringType extends Type
  case object BoolType extends ValueType

  case class PointerType(t: Type) extends Type with LLVMType {
    override def toString: String = s"${t.toString}*"

    override def deref: Type = t
  }


  case class FunctionType(returnType: Type, argsType: Seq[Type]) extends Type
}
