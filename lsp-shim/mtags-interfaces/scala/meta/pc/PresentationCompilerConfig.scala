package scala.meta.pc

import java.nio.file.{Path, Paths}
import java.util.{Arrays, HashMap, List as JList, Map as JMap, Optional}
import java.util.concurrent.TimeUnit

/** Configuration options used by the Metals presentation compiler. */
trait PresentationCompilerConfig {

  /** Command ID to trigger parameter hints (textDocument/signatureHelp) in the editor. */
  def parameterHintsCommand(): Optional[String]

  /** Command ID to trigger completions (textDocument/completion) in the editor. */
  def completionCommand(): Optional[String]

  def symbolPrefixes(): JMap[String, String]

  /** Returns true if user didn't modify symbol prefixes */
  def isDefaultSymbolPrefixes(): Boolean

  /** What text format to use for rendering `override def` labels for completion items. */
  def overrideDefFormat(): PresentationCompilerConfig.OverrideDefFormat

  def isCompletionItemDetailEnabled(): Boolean
  def isStripMarginOnTypeFormattingEnabled(): Boolean
  def isCompletionItemDocumentationEnabled(): Boolean
  def isHoverDocumentationEnabled(): Boolean
  def snippetAutoIndent(): Boolean
  def isSignatureHelpDocumentationEnabled(): Boolean
  def isCompletionSnippetsEnabled(): Boolean

  def isDetailIncludedInLabel(): Boolean = true

  /** The maximum delay for requests to respond. */
  def timeoutDelay(): Long

  def timeoutUnit(): TimeUnit

  def semanticdbCompilerOptions(): JList[String]

  def hoverContentType(): ContentType = ContentType.MARKDOWN

  // Added to the real mtags-interfaces interface after the version this
  // port was originally based on (found missing via a real-jar javap
  // diff, not present in the ~1.6.5-era sibling clone this module was
  // ported from) -- none of these 4 are referenced anywhere in
  // presentation-compiler's own source (confirmed via grep), so any safe
  // default is fine; kept here only for interface-shape completeness.
  def emitDiagnostics(): Boolean = true
  def workspaceRoot(): Path = Paths.get("").toAbsolutePath
  def sourcePathMode(): SourcePathMode = SourcePathMode.DISABLED
  def shouldRunRefchecks(): Boolean = false
  def scalaImportsPlacement(): PresentationCompilerConfig.ScalaImportsPlacement =
    PresentationCompilerConfig.ScalaImportsPlacement.APPEND_LAST
}

object PresentationCompilerConfig {
  enum OverrideDefFormat:
    /** Render as "override def". */
    case Ascii
    /** Render as a unicode glyph. */
    case Unicode

  enum ScalaImportsPlacement:
    case APPEND_LAST
    case SMART

  def defaultSymbolPrefixes(): JMap[String, String] = {
    val map = new HashMap[String, String]()
    map.put("scala/collection/mutable/", "mutable.")
    map.put("java/util/", "ju.")
    map
  }

  def defaultSemanticdbCompilerOptions(): JList[String] =
    Arrays.asList("-P:semanticdb:synthetics:on", "-P:semanticdb:text:on")
}
