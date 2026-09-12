package scala.meta.pc

/** Parameters for a presentation compiler request at a given range in a single source file. */
trait RangeParams extends OffsetParams {
  def endOffset(): Int
}
