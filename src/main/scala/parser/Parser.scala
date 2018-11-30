package parser

import compiler.CompileException

case class ParseError(lineNumber: Int, near: String, errorMsg: String) extends CompileException

trait Parser[T] {
  type ParseResult = Either[ParseError, T]
  def parse(content: String): ParseResult
}
