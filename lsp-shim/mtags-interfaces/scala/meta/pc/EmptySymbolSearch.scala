package scala.meta.pc

import java.net.URI
import java.util.{Collections, Optional}

/** A no-op `SymbolSearch` that never finds anything.
 *
 * NOTE: this class does not exist in the real `mtags-interfaces` module (confirmed
 * by reading its real source, version 1.6.7 -- there is no built-in "empty" search
 * implementation there). Added here so a thin LSP layer can construct a
 * `PresentationCompiler` with a minimal, always-available `SymbolSearch`, matching
 * the same minimal-config pattern the real presentation-compiler's own test harness
 * (`BasePCSuite`) uses.
 */
class EmptySymbolSearch extends SymbolSearch {
  override def documentation(symbol: String, parents: ParentSymbols): Optional[SymbolDocumentation] =
    Optional.empty()

  override def definition(symbol: String, sourceUri: URI): java.util.List[org.eclipse.lsp4j.Location] =
    Collections.emptyList()

  override def definitionSourceToplevels(symbol: String, sourceUri: URI): java.util.List[String] =
    Collections.emptyList()

  override def search(query: String, buildTargetIdentifier: String, visitor: SymbolSearchVisitor): SymbolSearch.Result =
    SymbolSearch.Result.COMPLETE

  override def searchMethods(query: String, buildTargetIdentifier: String, visitor: SymbolSearchVisitor): SymbolSearch.Result =
    SymbolSearch.Result.COMPLETE
}
