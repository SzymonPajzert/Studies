package compiler

import language.LLVM
import language.LLVM.{JumpPoint, Subblock}

object NormalizeQuadSubblocks extends Compiler[LLVM.Code, LLVM.Code] {
  def mergeSubblocks: List[LLVM.CodeBlock] => List[LLVM.CodeBlock] = {
    case Subblock(a) :: Subblock(b) :: t => mergeSubblocks(Subblock(a ++ b) :: t)
    case a :: t => a :: mergeSubblocks(t)
    case Nil => List()
  }

  def removeEmpty: List[LLVM.CodeBlock] => List[LLVM.CodeBlock] = {
    case Nil => Nil
    case JumpPoint(_) :: Nil => Nil
    case JumpPoint(_) :: (jmpPoint : JumpPoint) :: trail => removeEmpty(jmpPoint :: trail)
    case a :: trail => a :: removeEmpty(trail)
  }

  override def compile(code: LLVM.Code): Either[CompileException, LLVM.Code] = {
    val newBlocks = for {
      block <- code.blocks
      mergedSubblocks = mergeSubblocks(block.code.toList)
    } yield block.copy(code = mergedSubblocks.toVector)

    Right(code.copy(blocks = newBlocks))
  }
}
