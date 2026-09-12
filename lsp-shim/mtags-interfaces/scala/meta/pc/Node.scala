package scala.meta.pc

/** A single semantic token result. */
trait Node {
  def start(): Int
  def end(): Int
  def tokenType(): Int
  def tokenModifier(): Int
}
