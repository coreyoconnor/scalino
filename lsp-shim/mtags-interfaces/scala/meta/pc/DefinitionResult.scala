package scala.meta.pc

import org.eclipse.lsp4j.Location

trait DefinitionResult {
  def symbol(): String
  def locations(): java.util.List[Location]
}
