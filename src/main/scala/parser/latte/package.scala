package parser

package object latte {
  type Latte = Seq[TopDefinition]
  type Block = Seq[Instruction]
}
