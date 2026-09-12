package scala.meta.pc

/** Class used for including possible parent symbols in a scaladoc search, used for lazy evaluation. */
abstract class ParentSymbols {
  def parents(): java.util.List[String]
}
