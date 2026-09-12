package scala.meta.pc

import java.util.{List as JList, Map as JMap, Optional}
import java.util.concurrent.TimeUnit

/** A minimal, always-available `PresentationCompilerConfig`.
 *
 * NOTE: this class does not exist in the real `mtags-interfaces` module (confirmed
 * by reading its real source, version 1.6.7 -- there is no `default()`/static-factory
 * built-in implementation there, only the interface plus two static helper methods on
 * `PresentationCompilerConfig` for symbol prefixes / semanticdb options, both reused
 * below). Added here so a thin LSP layer can construct a `PresentationCompiler`
 * without depending on Metals' own (much larger) `mtags` module.
 */
class DefaultPresentationCompilerConfig extends PresentationCompilerConfig {
  override def parameterHintsCommand(): Optional[String] = Optional.empty()
  override def completionCommand(): Optional[String] = Optional.empty()
  override def symbolPrefixes(): JMap[String, String] = PresentationCompilerConfig.defaultSymbolPrefixes()
  override def isDefaultSymbolPrefixes(): Boolean = true
  override def overrideDefFormat(): PresentationCompilerConfig.OverrideDefFormat =
    PresentationCompilerConfig.OverrideDefFormat.Ascii
  override def isCompletionItemDetailEnabled(): Boolean = true
  override def isStripMarginOnTypeFormattingEnabled(): Boolean = true
  override def isCompletionItemDocumentationEnabled(): Boolean = true
  override def isHoverDocumentationEnabled(): Boolean = true
  override def snippetAutoIndent(): Boolean = true
  override def isSignatureHelpDocumentationEnabled(): Boolean = true
  override def isCompletionSnippetsEnabled(): Boolean = false
  override def timeoutDelay(): Long = 20L
  override def timeoutUnit(): TimeUnit = TimeUnit.SECONDS
  override def semanticdbCompilerOptions(): JList[String] = PresentationCompilerConfig.defaultSemanticdbCompilerOptions()
}
