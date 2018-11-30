import backend.{Directory, FileUtil}
import parser.Parser

package object compiler {
  def withParser[T](parser: Parser[T]): Compiler[Directory, T] = {
    (directory) => {
      val fileContent = FileUtil.readFile(directory.sourceFile)
      for {
        instantCode <- parser.parse(fileContent)
      } yield instantCode
    }
  }
}
