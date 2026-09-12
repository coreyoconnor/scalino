package scala.meta.pc

/** Parameters for inlay hint request at a given range in a single source file */
trait InlayHintsParams extends RangeParams {
  def inferredTypes(): Boolean
  def typeParameters(): Boolean
  def implicitParameters(): Boolean
  def byNameParameters(): Boolean = false
  def implicitConversions(): Boolean
  def namedParameters(): Boolean = false
  def hintsXRayMode(): Boolean = false
  def hintsInPatternMatch(): Boolean = false
  def closingLabels(): Boolean = false
}
