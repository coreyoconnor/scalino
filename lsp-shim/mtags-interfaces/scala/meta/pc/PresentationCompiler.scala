package scala.meta.pc

import scala.meta.pc.reports.ReportContext

import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.InlayHint
import org.eclipse.lsp4j.SelectionRange

import java.net.URI
import java.nio.file.Path
import java.util.{Arrays, Collections, List as JList, Optional}
import java.util.concurrent.{CompletableFuture, ExecutorService, ScheduledExecutorService}

/** The public API of the presentation compiler.
 *
 * This API should remain
 */
abstract class PresentationCompiler {

  // ==============================
  // Language Server Protocol APIs.
  // ==============================

  /** Returns token informations from presentation compiler. */
  def semanticTokens(params: VirtualFileParams): CompletableFuture[JList[Node]] =
    CompletableFuture.completedFuture(Collections.emptyList())

  /** Returns code completions for the given source position.
   *
   * @implNote supports cancellation.
   */
  def complete(params: OffsetParams): CompletableFuture[CompletionList]

  /** Returns a fully resolved completion item with defined fields such as
   * `documentation` and `details` populated.
   *
   * @implNote does not support cancellation.
   */
  def completionItemResolve(item: CompletionItem, symbol: String): CompletableFuture[CompletionItem]

  /** Returns the parameter hints at the given source position.
   *
   * @implNote supports cancellation.
   */
  def signatureHelp(params: OffsetParams): CompletableFuture[SignatureHelp]

  /** Returns the type of the expression at the given position along with the
   * symbol of the referenced symbol.
   */
  def hover(params: OffsetParams): CompletableFuture[Optional[HoverSignature]]

  /** Checks if the symbol at given position can be renamed using presentation
   * compiler.
   */
  def prepareRename(params: OffsetParams): CompletableFuture[Optional[Range]]

  /** Renames the symbol at given position and all of its occurrences to `name`. */
  def rename(params: OffsetParams, name: String): CompletableFuture[JList[TextEdit]]

  /** Returns the definition of the symbol at the given position. */
  def definition(params: OffsetParams): CompletableFuture[DefinitionResult]

  /** Returns location of the expression's type definition at the given position. */
  def typeDefinition(params: OffsetParams): CompletableFuture[DefinitionResult]

  /** Returns the occurrences of the symbol under the current position in the
   * entire file.
   */
  def documentHighlight(params: OffsetParams): CompletableFuture[JList[DocumentHighlight]]

  /** Returns the references of the symbol under the current position in the target files. */
  def references(params: ReferencesRequest): CompletableFuture[JList[ReferencesResult]] =
    CompletableFuture.completedFuture(Collections.emptyList())

  /** Execute the given code action.
   * @deprecated Please use the code action with optional data.
   */
  @deprecated("Please use the code action with optional data.", "")
  def codeAction(params: OffsetParams, codeActionId: String, codeActionPayload: Object): CompletableFuture[JList[TextEdit]] =
    codeAction(params, codeActionId, Optional.of(codeActionPayload))

  /** Execute the given code action. */
  def codeAction[T](params: OffsetParams, codeActionId: String, codeActionPayload: Optional[T]): CompletableFuture[JList[TextEdit]] =
    CompletableFuture.completedFuture(Collections.emptyList())

  /** Returns the list of code actions supported by the current presentation compiler. */
  def supportedCodeActions(): JList[String] = Arrays.asList()

  /** Return decoded and pretty printed TASTy content for .scala or .tasty file. */
  def getTasty(targetUri: URI, isHttpEnabled: Boolean): CompletableFuture[String]

  /** Return the necessary imports for a symbol at the given position. */
  def autoImports(name: String, params: OffsetParams, isExtension: java.lang.Boolean): CompletableFuture[JList[AutoImportsResult]]

  /** Return the missing implements and imports for the symbol at the given position. */
  def implementAbstractMembers(params: OffsetParams): CompletableFuture[JList[TextEdit]]

  /** Return the missing implements and imports for the symbol at the given position. */
  def insertInferredType(params: OffsetParams): CompletableFuture[JList[TextEdit]]

  /** Return the text edits for inlining a value. */
  def inlineValue(params: OffsetParams): CompletableFuture[JList[TextEdit]] =
    CompletableFuture.supplyAsync(() =>
      throw new DisplayableException("Inline value is not available in this version of Scala")
    )

  /** Extract method in selected range. */
  def extractMethod(range: RangeParams, extractionPos: OffsetParams): CompletableFuture[JList[TextEdit]]

  /** Return named arguments for the apply method that encloses the given position.
   * May fail with a DisplayableException.
   */
  def convertToNamedArguments(params: OffsetParams, argIndices: JList[Integer]): CompletableFuture[JList[TextEdit]]

  /** The text contents of the given file changed. */
  def didChange(params: VirtualFileParams): CompletableFuture[JList[Diagnostic]]

  /** Returns decorations for missing type adnotations, inferred type parameters,
   * implicit parameters and conversions.
   */
  def inlayHints(params: InlayHintsParams): CompletableFuture[JList[InlayHint]] =
    CompletableFuture.completedFuture(Collections.emptyList())

  /** Returns decorations for missing type adnotations, inferred type parameters,
   * implicit parameters and conversions.
   */
  def syntheticDecorations(params: SyntheticDecorationsParams): CompletableFuture[JList[SyntheticDecoration]] =
    CompletableFuture.completedFuture(Collections.emptyList())

  def info(symbol: String): CompletableFuture[Optional[PcSymbolInformation]] =
    CompletableFuture.completedFuture(Optional.empty())

  /** File was closed. */
  def didClose(uri: URI): Unit

  /** Returns the Protobuf byte array representation of a SemanticDB
   * `TextDocument` for the given source.
   */
  def semanticdbTextDocument(filename: URI, code: String): CompletableFuture[Array[Byte]]

  /** Returns the Protobuf byte array representation of a SemanticDB
   * `TextDocument` for the given source.
   */
  def semanticdbTextDocument(params: VirtualFileParams): CompletableFuture[Array[Byte]] =
    semanticdbTextDocument(params.uri(), params.text())

  /** Return the selections ranges for the given positions. */
  def selectionRange(params: JList[OffsetParams]): CompletableFuture[JList[SelectionRange]]

  // =================================
  // Configuration and lifecycle APIs.
  // =================================

  /** Clean up resources and shutdown the presentation compiler. */
  def shutdown(): Unit

  /** Clean the symbol table and other mutable state in the compiler. */
  def restart(): Unit

  /** Set logger level for reports. */
  def withReportsLoggerLevel(level: String): PresentationCompiler = this

  /** Set build target name. */
  def withBuildTargetName(buildTargetName: String): PresentationCompiler = this

  /** Provide a SymbolSearch to extract docstrings, java parameter names and Scala
   * default parameter values.
   */
  def withSearch(search: SymbolSearch): PresentationCompiler

  /** Provide a custom executor service to run asynchronous cancellation or requests. */
  def withExecutorService(executorService: ExecutorService): PresentationCompiler

  /** Provide a custom scheduled executor service to schedule `Thread.stop()` for
   * unresponsive compiler instances.
   */
  def withScheduledExecutorService(scheduledExecutorService: ScheduledExecutorService): PresentationCompiler

  /** Provide custom configuration for features like signature help and completions. */
  def withConfiguration(config: PresentationCompilerConfig): PresentationCompiler

  /** Provide workspace root for features like ammonite script $file completions. */
  def withWorkspace(workspace: Path): PresentationCompiler

  /** Provide CompletionItemPriority for additional sorting completion items. */
  def withCompletionItemPriority(priority: CompletionItemPriority): PresentationCompiler = this

  /** Provide a reporting context for reporting errors. */
  def withReportContext(reportContext: ReportContext): PresentationCompiler = this

  /** Construct a new presentation compiler with the given parameters. */
  def newInstance(buildTargetIdentifier: String, classpath: JList[Path], options: JList[String]): PresentationCompiler

  // ==============================================
  // Internal methods - not intended for public use
  // ==============================================
  def diagnosticsForDebuggingPurposes(): JList[String]

  /** Returns false if the presentation compiler has not been used since the last reset. */
  def isLoaded(): Boolean

  /** Scala version for the current presentation compiler */
  def scalaVersion(): String

  def isJava(): Boolean = false

  def buildTargetId(): String = ""
}
