package scala.meta.pc

/** Parameters for inlay hint request at a given range in a single source file */
trait SyntheticDecorationsParams extends VirtualFileParams {
  def inferredTypes(): Boolean
  def typeParameters(): Boolean
  def implicitParameters(): Boolean
  def implicitConversions(): Boolean
  def closingLabels(): Boolean = false
}
