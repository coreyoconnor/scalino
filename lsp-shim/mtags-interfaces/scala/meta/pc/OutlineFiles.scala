package scala.meta.pc

trait OutlineFiles {
  /** Will this outline compilation be substitute for build server's compilation.
   * Used if the first compilation using build server is unsuccessful.
   */
  def isFirstCompileSubstitute(): Boolean

  /** Files that should be outline compiled before calculating result. */
  def files(): java.util.List[VirtualFileParams]
}
