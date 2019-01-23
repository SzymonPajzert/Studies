package compiler

import backend.{Directory, FileUtil}
import language.Type.{ClassType, FunctionType, Type}
import language.TypeInformation

sealed trait CompileException
case class ParseFailure(lineNumber: Int, near: String, errorMsg: String) extends CompileException

trait TypingFailure extends CompileException {
  def typeInformation: TypeInformation
}
case class UndefinedFunction(name: String, typeInformation: TypeInformation) extends TypingFailure
case class UndefinedVariable(name: String, typeInformation: TypeInformation) extends TypingFailure
case class DuplicateDefinition(name: String, typeInformation: TypeInformation) extends TypingFailure
case class FieldNotFound(name: String, classT: ClassType, expr: String = "<expr>", typeInformation: TypeInformation) extends TypingFailure // TODO get representation
case class MethodNotFound(name: String, classT: ClassType, expr: String = "<expr>", typeInformation: TypeInformation) extends TypingFailure // TODO get representation
case class WrongType(expected: Type, actual: Type, expr: String = "<expr>", typeInformation: TypeInformation) extends TypingFailure // TODO add representation and line number
case class WrongArgumentNumber(expected: Int, actual: Int, name: String, typeInformation: TypeInformation) extends TypingFailure
case class ClassUndefined(className: ClassType, typeInformation: TypeInformation) extends TypingFailure
case class FunctionVoidArgument(funName: String, argName: String, typeInformation: TypeInformation) extends TypingFailure
trait MainFailure extends CompileException
case object NoMainFunction extends MainFailure
case class WrongMainSignature(signature: FunctionType) extends MainFailure

trait ReturnFailure extends CompileException
case class MissingReturn(functionName: String) extends ReturnFailure
case class WrongReturnType(expected: Type, instead: Type) extends ReturnFailure

// TODO add <: Language
trait Compiler[A, B] {
  self =>

  def compile(code: A): Either[CompileException, B]

  def ~>[C](compiler: Compiler[B, C]): Compiler[A, C] =
    (code: A) => for {
      success <- self.compile(code)
      next <- compiler.compile(success)
    } yield next
}

// Registers every stage in
class DebugCompiler[T](val phaseName: String,
                       val wrapped: Compiler[Directory, T],
                       val phaseCounter: Int) extends Compiler[Directory, T] {
  import sext._

  override def compile(directory: Directory): Either[CompileException, T] = {
    val filename = s"${phaseCounter}_$phaseName"
    val returnValue = wrapped.compile(directory)
    returnValue match {
      case Right(value) => {
        FileUtil.saveToFile(value.toString, directory.subfile(filename))
        FileUtil.saveToFile(value.treeString, directory.subfile(filename + ".pretty"))
      }
      case Left(error) => FileUtil.saveToFile(error.toString, directory.subfile(filename + ".err"))
    }

    returnValue
  }

  def nextStage[C](stageName: String, compiler: Compiler[T, C]): DebugCompiler[C] = {
    new DebugCompiler[C](stageName, this ~> compiler, phaseCounter + 1)
  }
}

object Compiler {
  def debug[T](name: String, compiler: Compiler[Directory, T]): DebugCompiler[T] =
    new DebugCompiler[T](name, compiler, 0)
}
