package parser

import backend.{Directory, FileUtil}
import compiler.CompileException
import compiler.Compiler

case class ParseError(lineNumber: Int, near: String, errorMsg: String) extends CompileException

trait Parser[T] extends Compiler[Directory, T] {
  type ParseResult = Either[List[ParseError], T]
  def parse(content: String): ParseResult

  def compile(directory: Directory) = {
    val fileContent = FileUtil.readFile(directory.sourceFile)
    for {
      instantCode <- parse(fileContent)
    } yield instantCode
  }
}
