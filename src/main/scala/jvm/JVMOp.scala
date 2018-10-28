package jvm

case class JVMClass(name: String)

sealed trait JVMOp

object JVMOp {
  type Block = List[JVMOp]

  type IsBasicType[T] = String

  implicit def integerIsSupported: IsBasicType[Int] = "i"


  def oneToAssembly(op: JVMOp): String = {
    op match {
      case GetStatic(name) => "getstatic  " + name
      case Ldc(value) => s"""ldc "$value""""
      case InvokeVirtual(name) => s"invokevirtual  $name"
      case Return => "return"
      case Const(value, "i") => s"bipush $value"
      case Add(typeId) => s"${typeId}add"
      case Load(frame, "i") => s"iload $frame"
      case Store(frame, "i") => s"istore $frame"
    }
  }

  def toAssembly(block: Block): String = {
    (block map oneToAssembly).mkString("", "\n  ", "")
  }

  def createMain(stackLimit: Int, block: Block): String = {
    s"""
       |.class  public Main
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
       |.limit stack ${stackLimit}
       |  ${toAssembly(block)}
       |.end method
       |""".stripMargin
  }

  /**
    * Prints last value on the stack
    * @return Operator printing last value on the stack
    */
  def printOperator: Block = List(
    GetStatic("java/lang/System/out Ljava/io/PrintStream;"),
    InvokeVirtual("java/io/PrintStream/println(I)V"))

  case class GetStatic(name: String) extends JVMOp

  case class Ldc(value: String) extends JVMOp

  case class InvokeVirtual(name: String) extends JVMOp

  case object Return extends JVMOp

  def add[T](implicit view: IsBasicType[T]): JVMOp = Add(view)

  case class Add(typeId: String) extends JVMOp

  def const[T](value: T)(implicit view: IsBasicType[T]): JVMOp = Const(value, view)

  case class Const[T](value: T, typeId: String) extends JVMOp

  case class Store(frame: Int, typeId: String) extends JVMOp

  def store[T](frame: Int)(implicit view: IsBasicType[T]): JVMOp = Store(frame, view)

  case class Load(frame: Int, typeId: String) extends JVMOp

  def load[T](frame: Int)(implicit view: IsBasicType[T]): JVMOp = Load(frame, view)
}


