package prover

import org.scalatest.FlatSpec

class ExecutorsTest extends FlatSpec {


  for (executor <- Executors.get) {
    behavior of executor.name

    it should "prove truth in simple env" in {
      val prove = Prove(Envs.simple, True)
      val result = executor.execute(prove)
      assert(result == Right(true))
    }
  }
}
