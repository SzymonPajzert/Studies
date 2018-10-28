package jvm

import org.scalatest.FlatSpec

class JVMOpSpec extends FlatSpec {
  import Definitions.validCode
  import JVMOp._

  behavior of "JVMOpSpec"

  it should "generate valid code" in {
    val operations = List(
      GetStatic("java/lang/System/out Ljava/io/PrintStream;"),
      Ldc("Hello world"),
      InvokeVirtual("java/io/PrintStream/println(Ljava/lang/String;)V"),
      Return
    )

    assert(JVMOp.createMain(2, operations) == validCode)
  }
}
