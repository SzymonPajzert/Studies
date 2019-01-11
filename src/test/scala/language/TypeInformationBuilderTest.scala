package language

import language.Type._
import org.scalatest.FlatSpec

class TypeInformationBuilderTest extends FlatSpec {
  behavior of "Type Information Builder"

  val base = ClassType("base")
  val derived = ClassType("derived")

  it should "detect derived classes after base is created" in {
    val builder = TypeInformation.builder

    builder.addClassStructure(base, None, Seq(), Seq(("baseMethods", FunctionType(IntType, Seq()))))
  }
}
