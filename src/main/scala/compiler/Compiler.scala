package compiler

trait Compiler[A, B] {
  self =>

  def compile(code: A): B

  def ~>[C](compiler: Compiler[B, C]): Compiler[A, C] =
    (code: A) => compiler.compile(self.compile(code))
}
