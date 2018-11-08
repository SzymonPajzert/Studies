package compiler

import backend.{Directory, FileUtil}
import parser.{InstantProg, Parser}

object DirectoryToInstant extends Compiler[Directory, InstantProg] {
  override def compile(code: Directory): InstantProg = {
    val fileContent = FileUtil.readFile(code.sourceFile)
    val Left(instantCode) = Parser.parse(fileContent)
    instantCode
  }
}
