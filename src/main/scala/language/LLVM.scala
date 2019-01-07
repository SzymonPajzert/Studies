package language

import java.io.File

import language.Type.PointerType

import scala.language.implicitConversions

object LLVM extends Language {
  import language.Type.{Type, IntType, VoidType}
  LanguageRegister.register(LLVM)

  case class Code(globalCode: String, blocks: List[Block])
  case class FunctionId(name: String, returnType: Type)
  case class Block(funcId: FunctionId, args: List[RegisterT], code: Vector[CodeBlock])

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
  case class Register[+T <: Type](value: String, typeId: Type, deref: String) extends Expression {
    def name: String = s"$deref$value"
  }

  def register[T <: Type](value: String, typeId: Type): Register[T] = new Register(value, typeId, "%")
  def constRegister[T <: Type](value: String, typeId: Type): Register[T] = new Register(value, typeId, "@.")

  type RegisterT = Register[Type]

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
                               indices: List[Expression] = List(Value("0", IntType), Value("0", IntType)),
                               sourceTypeOpt: Option[Type] = None): Func[T] = new Func[T] {
    override def getLine: String = {
      val sourceType = sourceTypeOpt.getOrElse(PointerType(elementType))
      val accessTypes = s"${elementType.llvmRepr}, ${sourceType.llvmRepr}"
      val indicesStr = (indices map (index => s"${index.typeId.llvmRepr} ${index.name}")).mkString(", ")
      s"getelementptr $accessTypes ${pointer.name}, $indicesStr"
    }
  }

  def alloca(t: Type, size: Option[Expression] = None): Func[t.type] = new Func[t.type] {
    override def getLine: String = size match {
      case None => s"alloca ${t.llvmRepr}"
      case Some(s) => s"alloca ${t.llvmRepr}, ${s.typeId.llvmRepr} ${s.name}"
    }
  }
  def load[T <: Type](valueLocation: Register[T]): Func[T] = new Func[T] {
    override def getLine: String = s"load ${valueLocation.typeId.deref.llvmRepr}, ${valueLocation.typeId.llvmRepr} ${valueLocation.name}"
  }
  def icmpSgt(left: Expression, right: Expression): Func[Nothing] = new Func[Nothing] {
    override def getLine: String = s"icmp sgt ${left.typeId.llvmRepr} ${left.name}, ${right.name}"
  }
  def expression(expr: Expression): Func[Nothing] = new Func[Nothing] {
    override def getLine: String = s"add i32 0, ${expr.name}"
  }
  def phi(blocks: (JumpPoint, Expression)*): Func[Nothing] = new Func[Nothing] {
    def elts: String = (blocks map {
      case (jmpPoint, value) => s"[${value.name}, %${jmpPoint.name}]"
    }).mkString(", ")

    override def getLine: String = {
      s"phi ${blocks.head._2.typeId.llvmRepr} $elts"
    }
  }

  sealed trait Instruction
  case class JumpIf(expression: Expression, ifTrue: JumpPoint, ifFalse: JumpPoint) extends Instruction
  case class Assign[T <: Type](destination: Register[T], function: Func[T]) extends Instruction
  case class AssignFuncall(destination: RegisterT, functionId: FunctionId, args: List[Expression]) extends Instruction
  case class AssignOp(destination: RegisterT, op: Operation, left: Expression, right: Expression) extends Instruction
  case class PrintInt(expression: Expression) extends Instruction
  case class Return(expression: Option[Expression]) extends Instruction
  case class Literal(code: String) extends Instruction

  def empty: Code = Code("", List())

  def runtimeLocation: File = {
    new File("/home/svp/Programming/mrjp/deps/runtime.bc")
  }

  def convertArgs(expressions: List[LLVM.Expression]): String = {
    (expressions map (exp => s"${exp.typeId.llvmRepr} ${exp.name}")).mkString(", ")
  }

  def serializeInstruction(instruction: Instruction): Either[String, (String, String)] = instruction match {
    case Literal(code) => Left(code)
    case JumpIf(expression, ifTrue, ifFalse) if expression.isConstTrue =>
      Left(s"br label %${ifTrue.name}")

    case JumpIf(expression, ifTrue, ifFalse) =>
      Left(s"br i1 ${expression.name}, label %${ifTrue.name}, label %${ifFalse.name}")

    case Assign(destination, code) =>
      Right(s"${destination.name} = ${code.getLine}", s"${destination.typeId.llvmRepr}")

    case AssignFuncall(_, functionId, arguments) if functionId.returnType == VoidType =>
      Left(s"call ${functionId.returnType.llvmRepr} @${functionId.name}(${convertArgs(arguments)})")

    case AssignFuncall(destination, functionId, arguments) =>
      Left(s"${destination.name} = call ${functionId.returnType.llvmRepr} @${functionId.name}(${convertArgs(arguments)})")

    case PrintInt(expression) =>
      Left(s"""call void @printInt(${expression.typeId.llvmRepr} ${expression.name})""")

    case Return(None) => Left(s"ret void")
    case Return(Some(expression)) => Left(s"ret ${expression.typeId.llvmRepr} ${expression.name}")
  }

  def serializeCodeBlock(codeBlock: CodeBlock): String = codeBlock match {
    case Subblock(instructions) => {
      val serializedWithGaps = instructions map serializeInstruction
      val maxWidth = (serializedWithGaps map {
        case Left(a) => a.length
        case Right((a, b)) => a.length
      }).max + 2

      val serialized = serializedWithGaps map {
        case Left(a) => a
        case Right((a, b)) => a + (" " * (maxWidth - a.length)) + "; " + b
      }

      (serialized map ("  " + _)).mkString("\n")
    }
    case JumpPoint(name) => s"\n$name:"
  }

  def makeArgs(args: List[RegisterT]): String =
    (args map {register => s"${register.typeId.llvmRepr} ${register.name}"}).mkString(", ")

  def mergeSubblocks(blocks: List[LLVM.CodeBlock]): Vector[LLVM.CodeBlock] =
    blocks match {
      case Subblock(a) :: Subblock(b) :: t => mergeSubblocks(Subblock(a ++ b) :: t)
      case a :: t => (a :: mergeSubblocks(t).toList).toVector
      case Nil => Vector()
    }

  def serializeBlock(block: Block): String = {
    s"""
       |define ${block.funcId.returnType.llvmRepr} @${block.funcId.name}(${makeArgs(block.args)}) {
       |${(mergeSubblocks(block.code.toList) map serializeCodeBlock).mkString("\n")}
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