package parser

import backend.{Directory, FileUtil}
import compiler.{Compiler, ParseFailure}

trait Parser[T] extends Compiler[Directory, T] {
  type ParseResult = Either[ParseFailure, T]
  def parse(content: String): ParseResult

  def compile(directory: Directory): ParseResult = {
    val fileContent = FileUtil.readFile(directory.sourceFile)
    for {
      instantCode <- parse(fileContent)
    } yield instantCode
  }
}
