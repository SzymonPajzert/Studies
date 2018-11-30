package language

import java.io.File

import scala.language.implicitConversions

object LLVM extends Language {
  import language.Type.{LLVMType => Type, IntType, ValueType, VoidType}

  case class Code(globalCode: String, blocks: List[Block])
  case class FunctionId(name: String, returnType: Type)
  case class Block(funcId: FunctionId, code: Vector[CodeBlock])

  trait CodeBlock
  case class JumpPoint(name: String) extends CodeBlock
  case class Subblock(instructions: Vector[Instruction]) extends CodeBlock


  sealed trait Expression {
    def name: String
    def typeId: Type
  }

  implicit def intToValue(int: Int): Expression = Value(Integer.toString(int), IntType)
  def value(int: Int): Value = Value(Integer.toString(int), IntType)
  case class Value(name: String, typeId: Type) extends Expression
  case class Register[+T <: Type](value: String, typeId: Type) extends Expression {
    def name: String = s"%$value"
  }

  type RegisterT = Register[Type]
  type ValueRegister = Register[ValueType]

  sealed trait Operation { def name: String }
  case object Add extends Operation { val name = "add" }
  case object Mul extends Operation { val name = "mul" }
  case object Div extends Operation { val name = "sdiv" }
  case object Sub extends Operation { val name = "sub" }

  sealed trait Instruction
  case class Jump(place: JumpPoint) extends Instruction
  case class JumpIf(expression: Expression, ifTrue: JumpPoint, ifFalse: JumpPoint) extends Instruction
  case class AssignCode(destination: RegisterT, code: String) extends Instruction
  case class AssignRegister(destination: RegisterT, source: Int) extends Instruction
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
    case Jump(place) =>
      s"br label %${place.name}"
    case JumpIf(expression, ifTrue, ifFalse) =>
      s"br i1 ${expression.name}, label %${ifTrue.name}, label %${ifFalse.name}"
    case AssignCode(destination, code) => s"${destination.name} = $code"
    case AssignFuncall(destination, functionId, arguments) if functionId.returnType == VoidType =>
      s"call ${functionId.returnType} @${functionId.name}(${convertArgs(arguments)})"
    case AssignFuncall(destination, functionId, arguments) => s"${destination.name} = call ${functionId.returnType} @${functionId.name}(${convertArgs(arguments)})"
    case AssignRegister(destination, source) =>
      s"${destination.name} = add ${source.typeId} 0, ${source.name}"
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
       |  ${(block.code map serializeCodeBlock).mkString("\n")}
       |}""".stripMargin
  }

  def serializeCode(code: Code): String = {
    s"""
       |declare void @printInt(i32)
       |declare void @printString(i8*)
       |
       |${code.globalCode}
       |
       |${(code.blocks map serializeBlock).mkString("\n\n")}
    """.stripMargin

  }
}