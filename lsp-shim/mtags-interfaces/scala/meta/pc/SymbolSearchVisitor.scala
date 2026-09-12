package scala.meta.pc

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SymbolKind

import java.nio.file.Path

/** Consumer of symbol search results.
 *
 * Search results can come from two different sources: classpath or workspace.
 * Classpath results are symbols defined in library dependencies while workspace
 * results are symbols that are defined by the user.
 */
abstract class SymbolSearchVisitor {
  def shouldVisitPackage(pkg: String): Boolean
  def visitClassfile(pkg: String, filename: String): Int
  def visitWorkspaceSymbol(path: Path, symbol: String, kind: SymbolKind, range: Range): Int
  def isCancelled(): Boolean

  def visitWorkspacePackage(pkg: String): Int = 0
}
