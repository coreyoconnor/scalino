package scala.meta.pc

import org.eclipse.lsp4j.Location

import java.net.URI
import java.util.Optional

/** The interface for the presentation compiler to extract symbol documentation and perform fuzzy symbol search. */
trait SymbolSearch {

  /** Returns the documentation of this symbol, if any. */
  def documentation(symbol: String, parents: ParentSymbols): Optional[SymbolDocumentation]

  /** Returns the documentation of this symbol, if any. */
  def documentation(symbol: String, parents: ParentSymbols, contentType: ContentType): Optional[SymbolDocumentation] =
    documentation(symbol, parents)

  /** Returns the definition of this symbol, if any. */
  def definition(symbol: String, sourceUri: URI): java.util.List[Location]

  /** Returns the all symbols in the file where the given symbol is defined
   * in declaration order, if any.
   */
  def definitionSourceToplevels(symbol: String, sourceUri: URI): java.util.List[String]

  /** Runs fuzzy symbol search for the given query. */
  def search(query: String, buildTargetIdentifier: String, visitor: SymbolSearchVisitor): SymbolSearch.Result

  /** Runs fuzzy symbol search for the given query, optionally filtering by toplevel member kind. */
  def search(
      query: String,
      buildTargetIdentifier: String,
      kind: Optional[MemberKind],
      visitor: SymbolSearchVisitor
  ): SymbolSearch.Result =
    search(query, buildTargetIdentifier, visitor)

  def searchMethods(query: String, buildTargetIdentifier: String, visitor: SymbolSearchVisitor): SymbolSearch.Result
}

object SymbolSearch {
  enum Result:
    case COMPLETE, INCOMPLETE
}
