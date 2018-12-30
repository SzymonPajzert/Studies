package language

import java.io.File

import language.Type.PointerType

import scala.language.implicitConversions

object LLVM extends Language {
  import language.Type.{LLVMType => Type, IntType, ValueType, VoidType}
  LanguageRegister.register(LLVM)

  case class Code(globalCode: String, blocks: List[Block])
  case class FunctionId(name: String, returnType: Type)
  case class Block(funcId: FunctionId, code: Vector[CodeBlock])

  trait CodeBlock
  case class JumpPoint(name: String) extends CodeBlock
  case class Subblock(instructions: Vector[Instruction]) extends CodeBlock

  sealed trait Expression {
    def name: String
    def typeId: Type
    def isConstTrue: Boolean = false
  }

  implicit def intToValue(int: Int): Expression = Value(Integer.toString(int), IntType)
  def value(int: Int): Value = Value(Integer.toString(int), IntType)

  case object ConstTrue extends Expression {
    override def isConstTrue: Boolean = true
    override def name: String = ???
    override def typeId: Type = ???
  }
  case class Value(name: String, typeId: Type) extends Expression
  class Register[+T <: Type](val value: String, val typeId: Type, val deref: String) extends Expression {
    def name: String = s"${deref}${value}"
  }

  def register[T <: Type](value: String, typeId: Type): Register[T] = new Register(value, typeId, "%")
  def constRegister[T <: Type](value: String, typeId: Type): Register[T] = new Register(value, typeId, "@.")

  type RegisterT = Register[Type]
  type ValueRegister = Register[ValueType]

  sealed trait Operation { def name: String }
  case object Add extends Operation { val name = "add" }
  case object Mul extends Operation { val name = "mul" }
  case object Div extends Operation { val name = "sdiv" }
  case object Sub extends Operation { val name = "sub" }

  def failJumpPoint: JumpPoint = JumpPoint("fail")
  def jump(point: JumpPoint) = JumpIf(ConstTrue, point, failJumpPoint)

  trait Func[+T <: Type] {
    def getLine: String
  }

  // TODO it should check types of the incomming registers
  def getElementPtr[T <: Type](elementType: Type,
                               pointer: Register[T],
                               indices: List[Expression] = List(Value("0", IntType), Value("0", IntType))): Func[T] = new Func[T] {
    override def getLine: String = {
      val accessTypes = s"$elementType, ${PointerType(elementType)}"
      val indicesStr = (indices map (index => s"${index.typeId} ${index.name}")).mkString(", ")
      s"getelementptr $accessTypes ${pointer.name}, $indicesStr"
    }
  }

  def alloca(t: Type, size: Option[Int] = None): Func[t.type] = new Func[t.type] {
    override def getLine: String = size match {
      case None => s"alloca $t"
      case Some(s) => s"alloca $t, i32 $s"
    }
  }
  def load[T <: Type](valueLocation: Register[T]): Func[T] = new Func[T] {
    override def getLine: String = s"load ${valueLocation.typeId.deref}, ${valueLocation.typeId} ${valueLocation.name}"
  }
  def icmpSgt(left: Expression, right: Expression): Func[Nothing] = new Func[Nothing] {
    override def getLine: String = s"icmp sgt ${left.typeId} ${left.name}, ${right.name}"
  }
  def expression(expr: Expression): Func[Nothing] = new Func[Nothing] {
    override def getLine: String = s"add i32 0, ${expr.name}"
  }

  sealed trait Instruction
  case class JumpIf(expression: Expression, ifTrue: JumpPoint, ifFalse: JumpPoint) extends Instruction
  case class Assign[T <: Type](destination: Register[T], function: Func[T]) extends Instruction
  case class AssignFuncall(destination: RegisterT, functionId: FunctionId, args: List[Expression]) extends Instruction
  case class AssignOp(destination: RegisterT, op: Operation, left: Expression, right: Expression) extends Instruction
  case class PrintInt(expression: Expression) extends Instruction
  case class Return(expression: Expression) extends Instruction
  case class Literal(code: String) extends Instruction

  def empty: Code = Code("", List())

  def runtimeLocation: File = {
    new File("/home/svp/Programming/mrjp/deps/runtime.bc")
  }

  def convertArgs(expressions: List[LLVM.Expression]): String = {
    (expressions map (exp => s"${exp.typeId} ${exp.name}")).mkString(", ")
  }

  def serializeInstruction(instruction: Instruction): String = instruction match {
    case Literal(code) => code
    case JumpIf(expression, ifTrue, ifFalse) if expression.isConstTrue =>
      s"br label %${ifTrue.name}"
    case JumpIf(expression, ifTrue, ifFalse) =>
      s"br i1 ${expression.name}, label %${ifTrue.name}, label %${ifFalse.name}"
    case Assign(destination, code) => s"${destination.name} = ${code.getLine}  ; ${destination.typeId}"
    case AssignFuncall(_, functionId, arguments) if functionId.returnType == VoidType =>
      s"call ${functionId.returnType} @${functionId.name}(${convertArgs(arguments)})"
    case AssignFuncall(destination, functionId, arguments) => s"${destination.name} = call ${functionId.returnType} @${functionId.name}(${convertArgs(arguments)})"
    case AssignOp(destination, op, left, right) =>
      s"${destination.name} = ${op.name} ${destination.typeId} ${left.name}, ${right.name}"
    case PrintInt(expression) =>
      s"""call void @printInt(${expression.typeId} ${expression.name})"""
    case Return(expression) =>
      s"""ret ${expression.typeId} ${expression.name}"""
  }

  def serializeCodeBlock(codeBlock: CodeBlock): String = codeBlock match {
    case Subblock(instructions) => (instructions map ("  " + serializeInstruction(_))).mkString("\n")
    case JumpPoint(name) => s"\n$name:"
  }

  def serializeBlock(block: Block): String = {
    s"""
       |define ${block.funcId.returnType} @${block.funcId.name}() {
       |${(block.code map serializeCodeBlock).mkString("\n")}
       |}""".stripMargin
  }

  def serializeCode(code: Code): String = {
    s"""
       |declare void @printInt(i32)
       |declare void @printString(i8*)
       |
       |; begin global section
       |${code.globalCode}
       |; end global section
       |
       |${(code.blocks map serializeBlock).mkString("\n\n")}
    """.stripMargin

  }
}