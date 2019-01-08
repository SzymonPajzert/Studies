package compiler

import backend.{Directory, FileUtil}

trait CompileException
// TODO remove and substitute with errors
case class ErrorString(message: String) extends CompileException

// TODO add <: Language
trait Compiler[A, B] {

  self =>

  trait ResultWithFail[F, G]
  case class Good[F, G](result: G) extends ResultWithFail[F, G]
  case class Fail[F, G](result: G, fail: F) extends ResultWithFail[F, G]

  def compile(code: A): Either[List[CompileException], B]

  def ~>[C](compiler: Compiler[B, C]): Compiler[A, C] =
    (code: A) => for {
      success <- self.compile(code)
      next <- compiler.compile(success)
    } yield next
}

// Registers every stage in
class DebugCompiler[T](val phaseName: String, val wrapped: Compiler[Directory, T]) extends Compiler[Directory, T] {
  import sext._

  override def compile(directory: Directory): Either[List[CompileException], T] = {
    val returnValue = wrapped.compile(directory)
    returnValue match {
      case Right(value) => {
        FileUtil.saveToFile(value.toString, directory.subfile(phaseName))
        FileUtil.saveToFile(value.treeString, directory.subfile(phaseName + ".pretty"))
      }
      case Left(error) => FileUtil.saveToFile(error.toString, directory.subfile(phaseName + ".err"))
    }

    returnValue
  }

  def nextStage[C](stageName: String, compiler: Compiler[T, C]): DebugCompiler[C] = {
    Compiler.debug(stageName, this ~> compiler)
  }
}

object Compiler {
  def debug[T](name: String, compiler: Compiler[Directory, T]): DebugCompiler[T] =
    new DebugCompiler[T](name, compiler)
}
