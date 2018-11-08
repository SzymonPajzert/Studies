package backend.jvm

case class JVMClass(name: String)

sealed trait JVMOp

object JVMOp {
  case class Code(stackSize: Int, localVariables: Int, code: Block)

  type Block = List[JVMOp]

  trait IsBasicType[+T] {
    def name: String
  }

  implicit object IntegerIsSupported extends IsBasicType[Int] {
    val name = "i"
  }

  implicit object CharIsSupported extends IsBasicType[Char] {
    val name = "c"
  }

  def oneToAssembly(op: JVMOp): String = {
    op match {
      case GetStatic(name) => "getstatic  " + name
      case Ldc(value) => s"""ldc "$value""""
      case InvokeVirtual(name) => s"invokevirtual  $name"
      case Return => "return"
      case Const(value, CharIsSupported) => s"bipush ${Char.char2int(value.asInstanceOf[Char])}"
      case Const(value, IntegerIsSupported) => s"ldc $value"
      case Add(typeId) => s"${typeId}add"
      case Mul(typeId) => s"${typeId}mul"
      case Div(typeId) => s"${typeId}div"
      case Sub(typeId) => s"${typeId}sub"
      case Load(frame, "i") => if (frame < 4) s"iload_$frame" else s"iload $frame"
      case Store(frame, "i") => if (frame < 4) s"istore_$frame" else s"istore $frame"
    }
  }

  def toAssembly(block: Block): String = {
    (block map oneToAssembly).mkString("", "\n  ", "")
  }

  def createMain(className: String, jvmCode: Code): String = {
    s"""
       |.class  public $className
       |.super  java/lang/Object
       |
       |; standard initializer
       |.method public <init>()V
       |  aload_0
       |  invokespecial java/lang/Object/<init>()V
       |  return
       |.end method
       |
       |.method public static main([Ljava/lang/String;)V
       |.limit stack ${jvmCode.stackSize}
       |.limit locals ${jvmCode.localVariables}
       |  ${toAssembly(jvmCode.code)}
       |.end method
       |""".stripMargin
  }

  case class GetStatic(name: String) extends JVMOp

  case class Ldc(value: String) extends JVMOp

  case class InvokeVirtual(name: String) extends JVMOp

  case object Return extends JVMOp

  def add[T](implicit view: IsBasicType[T]): JVMOp = Add(view.name)

  case class Add(typeId: String) extends JVMOp

  def mul[T](implicit view: IsBasicType[T]): JVMOp = Mul(view.name)

  case class Mul(typeId: String) extends JVMOp

  def div[T](implicit view: IsBasicType[T]): JVMOp = Div(view.name)

  case class Div(typeId: String) extends JVMOp

  def sub[T](implicit view: IsBasicType[T]): JVMOp = Sub(view.name)

  case class Sub(typeId: String) extends JVMOp

  def const[T](value: T)(implicit view: IsBasicType[T]): JVMOp = Const(value, view)

  case class Const[T](value: T, view: IsBasicType[T]) extends JVMOp

  case class Store(frame: Int, typeId: String) extends JVMOp

  def store[T](frame: Int)(implicit view: IsBasicType[T]): JVMOp = Store(frame, view.name)

  case class Load(frame: Int, typeId: String) extends JVMOp

  def load[T](frame: Int)(implicit view: IsBasicType[T]): JVMOp = Load(frame, view.name)
}


