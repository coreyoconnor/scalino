package scala.meta.pc

import org.eclipse.lsp4j.TextEdit

import java.util.Optional

trait AutoImportsResult {
  def packageName(): String
  def edits(): java.util.List[TextEdit]
  def symbol(): Optional[String] = Optional.empty()
}
