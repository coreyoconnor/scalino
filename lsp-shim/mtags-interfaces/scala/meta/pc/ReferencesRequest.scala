package scala.meta.pc

import org.eclipse.lsp4j.jsonrpc.messages.Either

import java.util.Collections

trait ReferencesRequest {
  def file(): VirtualFileParams
  def includeDefinition(): Boolean
  def offsetOrSymbol(): Either[Integer, String]
  def alternativeSymbols(): java.util.List[String] = Collections.emptyList()
}
