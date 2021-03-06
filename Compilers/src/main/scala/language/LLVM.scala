package language

import java.io.File

import backend.FileUtil
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
  case class Register(value: String, typeId: Type, deref: String) extends Expression {
    def name: String = s"$deref$value"
  }

  def register[T <: Type](value: String, typeId: Type): Register = new Register(value, typeId, "%")
  def constRegister[T <: Type](value: String, typeId: Type): Register = new Register(value, typeId, "@.")

  type RegisterT = Register

  sealed trait Operation { def name: String }
  case object Add extends Operation { val name = "add" }
  case object Mul extends Operation { val name = "mul" }
  case object Div extends Operation { val name = "sdiv" }
  case object Sub extends Operation { val name = "sub" }

  def failJumpPoint: JumpPoint = JumpPoint("fail")
  def jump(point: JumpPoint) = JumpIf(ConstTrue, point, failJumpPoint)

  case class Func(line: String)

  // TODO it should check types of the incomming registers
  def getElementPtr(elementType: Type,
                    pointer: Register,
                    indices: List[Expression] = List(Value("0", IntType), Value("0", IntType))): Func = Func {
    val sourceType = PointerType(elementType)
    val accessTypes = s"${elementType.llvmRepr}, ${sourceType.llvmRepr}"
    val indicesStr = (indices map (index => s"${index.typeId.llvmRepr} ${index.name}")).mkString(", ")
    s"getelementptr $accessTypes ${pointer.name}, $indicesStr"
  }

  def alloca(t: Type, size: Option[Expression] = None): Func = Func {
    size match {
      case None => s"alloca ${t.llvmRepr}"
      case Some(s) => s"alloca ${t.llvmRepr}, ${s.typeId.llvmRepr} ${s.name}"
    }
  }

  def load(valueLocation: Register): Func = Func {
    s"load ${valueLocation.typeId.deref.llvmRepr}, ${valueLocation.typeId.llvmRepr} ${valueLocation.name}"
  }
  def icmpSgt(left: Expression, right: Expression): Func = Func {
    s"icmp sgt ${left.typeId.llvmRepr} ${left.name}, ${right.name}"
  }
  def expression(expr: Expression): Func = Func {
    s"add i32 0, ${expr.name}"
  }
  def phi(blocks: (JumpPoint, Expression)*): Func = Func {
    def elts: String = (blocks map {
      case (jmpPoint, value) => s"[${value.name}, %${jmpPoint.name}]"
    }).mkString(", ")


    s"phi ${blocks.head._2.typeId.llvmRepr} $elts"
  }
  def call(returnT: Type, name: String, args: List[Expression]): Func = Func {
    s"call ${returnT.llvmRepr} $name(${LLVM.convertArgs(args)})"
  }

  sealed trait Instruction
  case class JumpIf(expression: Expression, ifTrue: JumpPoint, ifFalse: JumpPoint) extends Instruction
  case class Assign[T <: Type](destination: Register, function: Func) extends Instruction
  case class AssignFuncall[T <: Type](destination: RegisterT, retType: Type, func: Func) extends Instruction
  case class AssignOp(destination: RegisterT, op: Operation, left: Expression, right: Expression) extends Instruction
  // case class PrintInt(expression: Expression) extends Instruction
  case class Return(expression: Option[Expression]) extends Instruction
  case class Literal(code: String) extends Instruction

  def empty: Code = Code("", List())

  def runtimeLocation: File = {
    new File(FileUtil.root + "deps/runtime.bc")
  }

  def externalDepsLocation: File = {
    new File(FileUtil.root + "deps/external.bc")
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
      Right(s"${destination.name} = ${code.line}", s"${destination.typeId.llvmRepr}")

    case AssignFuncall(_, VoidType, func)  =>
      Left(func.line)

    case AssignFuncall(destination, _, func) =>
      Left(s"${destination.name} = ${func.line}")

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

  def serializeBlock(block: Block): String = {
    s"""
       |define ${block.funcId.returnType.llvmRepr} @${block.funcId.name}(${makeArgs(block.args)}) {
       |${(block.code.toList map serializeCodeBlock).mkString("\n")}
       |}""".stripMargin
  }

  def serializeCode(code: Code): String = {
    s"""
       |declare i32 @readInt()
       |declare i8* @readString()
       |declare void @printInt(i32)
       |declare void @printString(i8*)
       |declare void @error()
       |
       |declare i8* @string_concat(i8*, i8*)
       |declare i8* @malloc(i32)
       |
       |; begin global section
       |${code.globalCode}
       |; end global section
       |
       |${(code.blocks map serializeBlock).mkString("\n\n")}
    """.stripMargin

  }
}