package scala.meta.pc

/** Parameters for a presentation compiler request at a given offset in a single source file. */
trait OffsetParams extends VirtualFileParams {
  def offset(): Int
}
