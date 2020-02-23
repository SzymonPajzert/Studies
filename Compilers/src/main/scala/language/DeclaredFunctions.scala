package language

import language.Type._

object DeclaredFunctions {
  case class Func(name: String, t: Type) {
    def llvmRepresentation: String = ???
  }

  def all: Map[String, FunctionType] = Map(
    "error" -> FunctionType(VoidType, Seq()),
    "printInt" -> FunctionType(VoidType, Seq(IntType)),
    "printString" -> FunctionType(VoidType, Seq(StringType)),
    "readString" -> FunctionType(StringType, Seq()),
    "readInt" -> FunctionType(IntType, Seq())
  )

  def get(name: String): Func = Func(name, all(name))

  def defined(name: String): Boolean = all contains name
}