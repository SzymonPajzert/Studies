package backend.llvm

import java.io.File

import backend.{BackendRunner, CommandResult, FileUtil, OutputDirectory}
import parser.instant.Operation

import scala.language.implicitConversions

object LLVM {
  type Code = List[Instruction]

  sealed trait Type
  case object IntType extends Type {
    override def toString: String = "i32"
  }

  implicit def intToValue(int: Int): Expression = Value(Integer.toString(int), IntType)

  sealed trait Expression {
    def name: String
    def typeId: Type
  }

  def value(int: Int): Value = Value(Integer.toString(int), IntType)

  case class Value(name: String, typeId: Type) extends Expression
  case class Register(value: String, typeId: Type) extends Expression {
    def name: String = s"%$value"
  }

  sealed trait Operation { def name: String }
  case object Add extends Operation { val name = "add" }
  case object Mul extends Operation { val name = "mul" }
  case object Div extends Operation { val name = "sdiv" }
  case object Sub extends Operation { val name = "sub" }

  sealed trait Instruction
  case class AssignRegister(destination: Register, source: Register) extends Instruction
  case class AssignOp(destination: Register, op: Operation, left: Expression, right: Expression) extends Instruction
  case class PrintInt(expression: Expression) extends Instruction

  def empty: Code = List()

  def runtimeLocation: File = {
    new File("/home/svp/Programming/mrjp/deps/runtime.bc")
  }

  def serializeCode(code: Code): String = {
    def serializeInstruction(instruction: Instruction): String = instruction match {
      case AssignRegister(destination, source) =>
        s"${destination.name} = add ${source.typeId} 0, ${source.name}"
      case AssignOp(destination, op, left, right) =>
        s"${destination.name} = ${op.name} ${destination.typeId} ${left.name}, ${right.name}"
      case PrintInt(expression) =>
        s"""call void @printInt(${expression.typeId} ${expression.name})"""
    }

    s"""
      |@.newline = internal constant [2 x i8] c"\\0A\\00"
      |declare void @printInt(i32) ;  w innym module
      |declare void @printString(i8*)
      |define i32 @main() {
      |  %t0 = bitcast [2 x i8]* @.newline to i8*
      |  ${(code map serializeInstruction).mkString("\n  ")}
      |  ret i32 0
      |}
    """.stripMargin

  }
}

object LlvmRunner extends BackendRunner[LLVM.Code] {
  override def compile(code: LLVM.Code, outputDirectory: OutputDirectory): CommandResult[String] = {
    FileUtil.saveToFile(LLVM.serializeCode(code), outputDirectory.llvmFile)

    FileUtil.runCommand(
      s"""
         |llvm-as
         | -o ${outputDirectory.llvmTempExecutable}
         | ${outputDirectory.llvmFile}""".stripMargin.replaceAll("[\n]", ""))

    FileUtil.runCommand(
      s"""
         |llvm-link
         | -o ${outputDirectory.llvmExecutable}
         | ${outputDirectory.llvmTempExecutable} ${LLVM.runtimeLocation}
       """.stripMargin
    )

    FileUtil.runCommand(s"rm ${outputDirectory.llvmTempExecutable}")
  }

  override def run(outputDirectory: OutputDirectory): List[Int] = {
    FileUtil.runInst(s"lli ${outputDirectory.llvmExecutable}").stdout
  }
}
