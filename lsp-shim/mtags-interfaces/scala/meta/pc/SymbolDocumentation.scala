package scala.meta.pc

/** Documentation about a single symbol such as a class/method/parameter. */
trait SymbolDocumentation {
  def symbol(): String
  def displayName(): String
  def docstring(): String
  def defaultValue(): String
  def typeParameters(): java.util.List[SymbolDocumentation]
  def parameters(): java.util.List[SymbolDocumentation]
}
