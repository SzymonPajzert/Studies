package compiler

import language.Language

trait CompileException

// TODO add <: Language
trait Compiler[A, B] {
  self =>

  def compile(code: A): Either[List[CompileException], B]

  def ~>[C](compiler: Compiler[B, C]): Compiler[A, C] =
    (code: A) => for {
      success <- self.compile(code)
      next <- compiler.compile(success)
    } yield next
}
