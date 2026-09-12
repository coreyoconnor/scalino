package scala.meta.pc

import scala.meta.pc.reports.ReportContext

import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.CompletionTriggerKind
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.InlayHint
import org.eclipse.lsp4j.SelectionRange

import java.net.URI
import java.nio.file.Path
import java.util.{List as JList, Optional}

/** The raw public API of the presentation compiler that does not handle synchronisation.
 * Scala compiler can't run concurrent code at that point, so we need to enforce sequential,
 * single threaded execution. It has to be implemented by the consumer of this API.
 *
 * This API should remain binary compatible.
 */
abstract class RawPresentationCompiler {

  // ==============================
  // Language Server Protocol APIs.
  // ==============================

  def semanticTokens(params: VirtualFileParams): JList[Node]

  /** @implNote supports cancellation. */
  def complete(params: OffsetParams, completionTriggerKind: CompletionTriggerKind): CompletionList

  /** @implNote does not support cancellation. */
  def completionItemResolve(item: CompletionItem, symbol: String): CompletionItem

  /** @implNote supports cancellation. */
  def signatureHelp(params: OffsetParams): SignatureHelp

  def hover(params: OffsetParams): Optional[HoverSignature]

  def prepareRename(params: OffsetParams): Optional[Range]

  def rename(params: OffsetParams, name: String): JList[TextEdit]

  def definition(params: OffsetParams): DefinitionResult

  def typeDefinition(params: OffsetParams): DefinitionResult

  def documentHighlight(params: OffsetParams): JList[DocumentHighlight]

  def references(params: ReferencesRequest): JList[ReferencesResult]

  def codeAction[T](params: OffsetParams, codeActionId: String, codeActionPayload: Optional[T]): JList[TextEdit]

  def supportedCodeActions(): JList[String]

  def getTasty(targetUri: URI, isHttpEnabled: Boolean): String

  def autoImports(name: String, params: OffsetParams, isExtension: java.lang.Boolean): JList[AutoImportsResult]

  def implementAbstractMembers(params: OffsetParams): JList[TextEdit]

  def insertInferredType(params: OffsetParams): JList[TextEdit]

  def inlineValue(params: OffsetParams): JList[TextEdit]

  def extractMethod(range: RangeParams, extractionPos: OffsetParams): JList[TextEdit]

  def convertToNamedArguments(params: OffsetParams, argIndices: JList[Integer]): JList[TextEdit]

  def didChange(params: VirtualFileParams): JList[Diagnostic]

  def inlayHints(params: InlayHintsParams): JList[InlayHint]

  def info(symbol: String): Optional[PcSymbolInformation]

  def didClose(uri: URI): Unit

  def semanticdbTextDocument(filename: URI, code: String): Array[Byte]

  def semanticdbTextDocument(params: VirtualFileParams): Array[Byte]

  def selectionRange(params: JList[OffsetParams]): JList[SelectionRange]

  // =================================
  // Configuration and lifecycle APIs.
  // =================================

  def withReportsLoggerLevel(level: String): RawPresentationCompiler

  def withBuildTargetName(buildTargetName: String): RawPresentationCompiler

  def withSearch(search: SymbolSearch): RawPresentationCompiler

  def withConfiguration(config: PresentationCompilerConfig): RawPresentationCompiler

  def withWorkspace(workspace: Path): RawPresentationCompiler

  def withCompletionItemPriority(priority: CompletionItemPriority): RawPresentationCompiler

  def withReportContext(reportContext: ReportContext): RawPresentationCompiler

  def newInstance(buildTargetIdentifier: String, classpath: JList[Path], options: JList[String]): RawPresentationCompiler

  def scalaVersion(): String

  def buildTargetId(): String
}
