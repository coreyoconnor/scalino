package dotty.tools
package languageserver

import java.io.File
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

import dotty.tools.pc.RawScalaPresentationCompiler
import scala.meta.internal.metals.{CompilerOffsetParams, CompilerVirtualFileParams}
import scala.meta.internal.pc.PcReferencesRequest
import scala.meta.pc.{OffsetParams, VirtualFileParams}
import org.eclipse.lsp4j.jsonrpc.messages.{Either => JEither}
import org.eclipse.lsp4j as l

import com.github.plokhotnyuk.jsoniter_scala.core._
import Lsp._
import Lsp.given

/** Thin LSP layer over `scala3-presentation-compiler` (Metals' PC engine),
 *  reusing `Main.scala`'s existing JSON-RPC transport and `Lsp.scala`'s
 *  existing jsoniter-scala wire model -- no lsp4j, no Gson, no reflection,
 *  matching `DottyLanguageServer`'s own "no reflection anywhere" design (see
 *  its doc comment). Real lsp4j/mtags-interfaces/mtags-shared types are
 *  satisfied by the in-tree shim at `lsp-shim/` (not real jars) -- see
 *  `~/.claude/plans/lovely-jumping-anchor.md` for the full design.
 *
 *  Prototype scope: completion/hover/definition/references/rename/
 *  documentHighlight/signatureHelp + didOpen/didChange/didClose diagnostics.
 *  presentation-compiler has no documentSymbol/workspaceSymbol/implementation
 *  equivalent (those come from Metals' own BSP-driven indexer, not the PC
 *  itself) -- `Main.scala`'s `-pc` dispatch just doesn't wire those methods.
 */
class PcLanguageServer(publishDiagnostics: (String, List[Lsp.Diagnostic]) => Unit) { thisServer =>

  private var pc: RawScalaPresentationCompiler = null
  private val buffers: mutable.Map[URI, String] = mutable.Map.empty

  /** Same on-disk config `DottyLanguageServer` reads (`scalino setup-ide`'s
   *  output) -- reused so both backends work from the same project setup. */
  private def loadConfig(rootUri: String): List[ProjectConfig] = {
    val configFile = new File(new URI(rootUri + '/' + DottyLanguageServer.IDE_CONFIG_FILE))
    if (!configFile.exists)
      throw new java.io.FileNotFoundException(
        s"${DottyLanguageServer.IDE_CONFIG_FILE} not found at $rootUri -- run `scalino setup-ide <sources...>` in the project root first")
    readFromArray(Files.readAllBytes(configFile.toPath))(using projectConfigListCodec)
  }

  def initialize(rootUri: String): InitializeResult = thisServer.synchronized {
    val capabilities = ServerCapabilities(
      textDocumentSync = 1, // Full
      documentHighlightProvider = true,
      documentSymbolProvider = false,
      definitionProvider = true,
      renameProvider = true,
      hoverProvider = true,
      workspaceSymbolProvider = false,
      referencesProvider = true,
      implementationProvider = false,
      completionProvider = CompletionOptions(triggerCharacters = List(".")),
      signatureHelpProvider = SignatureHelpOptions(triggerCharacters = List("(")))

    val warmup = new Thread(() => {
      try {
        val configs = loadConfig(rootUri)
        val classpath: Seq[Path] =
          configs.flatMap(c => c.classDirectory +: c.dependencyClasspath).distinct.map(Paths.get(_))
        val sourceDirs: Seq[Path] =
          configs.flatMap(_.sourceDirectories).distinct.map(Paths.get(_))
        thisServer.synchronized {
          pc = RawScalaPresentationCompiler(
            buildTargetIdentifier = "scalino",
            classpath = classpath,
            options = Nil,
            sourcePath = () => sourceDirs.asJava
          )
        }
      } catch {
        case scala.util.control.NonFatal(ex) => ex.printStackTrace()
      }
    })
    warmup.setDaemon(true)
    warmup.start()

    InitializeResult(capabilities)
  }

  private def requirePc(): RawScalaPresentationCompiler = thisServer.synchronized {
    if (pc == null) throw new IllegalStateException("presentation compiler not yet initialized")
    pc
  }

  /** Convert an `Lsp.Position` (line/character) to a raw text offset --
   *  presentation-compiler's `OffsetParams` always wants a plain `Int`
   *  offset, computed from whatever full text the client last sent (no
   *  driver/SourceFile needed, unlike `DottyLanguageServer.sourcePosition`
   *  -- this is pure text math). */
  private def positionToOffset(text: String, pos: Position): Int = {
    var idx = 0
    var line = 0
    while (line < pos.line) {
      val nl = text.indexOf('\n', idx)
      if (nl < 0) return text.length
      idx = nl + 1
      line += 1
    }
    math.min(idx + pos.character, text.length)
  }

  private def textOf(uri: URI): String =
    buffers.getOrElse(uri, throw new IllegalStateException(s"no open buffer for $uri"))

  private def offsetParams(uri: URI, pos: Position): CompilerOffsetParams = {
    val text = textOf(uri)
    CompilerOffsetParams(uri, text, positionToOffset(text, pos))
  }

  private def toRange(r: l.Range): Range =
    Range(Position(r.getStart().getLine(), r.getStart().getCharacter()), Position(r.getEnd().getLine(), r.getEnd().getCharacter()))

  private def toLocation(l0: l.Location): Location =
    Location(l0.getUri(), toRange(l0.getRange()))

  private def toTextEdit(e: l.TextEdit): TextEdit =
    TextEdit(toRange(e.getRange()), e.getNewText())

  /** Real lsp4j types markup-ish fields as `Either<String, MarkupContent>`
   *  (or similar) since a raw string is also a valid MarkupContent -- unwrap
   *  either branch into a plain `MarkupContent`. */
  private def eitherToMarkupContent(e: JEither[String, l.MarkupContent]): MarkupContent =
    if (e == null) MarkupContent("plaintext", "")
    else if (e.isLeft()) MarkupContent("plaintext", e.getLeft())
    else { val mc = e.getRight(); MarkupContent(mc.getKind(), mc.getValue()) }

  private def eitherToString(e: JEither[String, ?]): String =
    if (e == null) "" else if (e.isLeft()) e.getLeft() else String.valueOf(e.getRight())

  def didOpen(params: DidOpenTextDocumentParams): Unit = thisServer.synchronized {
    val document = params.textDocument
    val uri = new URI(document.uri)
    buffers(uri) = document.text
    publishDiagnosticsFor(uri, document.uri)
  }

  def didChange(params: DidChangeTextDocumentParams): Unit = thisServer.synchronized {
    val document = params.textDocument
    val uri = new URI(document.uri)
    val change = params.contentChanges.head
    assert(change.range.isEmpty, "TextDocumentSyncKind.Incremental support is not implemented")
    buffers(uri) = change.text
    publishDiagnosticsFor(uri, document.uri)
  }

  private def publishDiagnosticsFor(uri: URI, uriString: String): Unit = {
    val diags = requirePc().didChange(CompilerVirtualFileParams(uri, textOf(uri)))
    val lspDiags = diags.asScala.map { d =>
      val severity = Option(d.getSeverity()).map(_.getValue()).getOrElse(1)
      Diagnostic(toRange(d.getRange()), eitherToString(d.getMessage()), severity, Option(d.getSource()).getOrElse(""), Option(d.getCode()).map(_.toString).getOrElse(""))
    }.toList
    publishDiagnostics(uriString, lspDiags)
  }

  def didClose(params: DidCloseTextDocumentParams): Unit = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    buffers.remove(uri)
    requirePc().didClose(uri)
  }

  def completion(params: TextDocumentPositionParams): CompletionList = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val result = requirePc().complete(offsetParams(uri, params.position), l.CompletionTriggerKind.Invoked)
    CompletionList(
      isIncomplete = result.isIncomplete(),
      items = result.getItems().asScala.map { item =>
        CompletionItem(
          label = item.getLabel(),
          kind = Option(item.getKind()).map(_.getValue()),
          detail = Option(item.getDetail()),
          documentation = Option(item.getDocumentation()).map(eitherToMarkupContent),
          deprecated = Option(item.getDeprecated()).exists(_.booleanValue)
        )
      }.toList
    )
  }

  def definition(params: TextDocumentPositionParams): List[Location] = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val result = requirePc().definition(offsetParams(uri, params.position))
    result.locations().asScala.map(toLocation).toList
  }

  def references(params: ReferenceParams): List[Location] = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val text = textOf(uri)
    val offset = positionToOffset(text, params.position)
    val request = PcReferencesRequest(
      CompilerVirtualFileParams(uri, text),
      params.context.includeDeclaration,
      JEither.forLeft(Integer.valueOf(offset))
    )
    val results = requirePc().references(request)
    results.asScala.flatMap(_.locations().asScala).map(toLocation).toList
  }

  def rename(params: RenameParams): WorkspaceEdit = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val edits = requirePc().rename(offsetParams(uri, params.position), params.newName)
    WorkspaceEdit(Map(params.textDocument.uri -> edits.asScala.map(toTextEdit).toList))
  }

  def documentHighlight(params: TextDocumentPositionParams): List[DocumentHighlight] = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val result = requirePc().documentHighlight(offsetParams(uri, params.position))
    result.asScala.map(h => DocumentHighlight(toRange(h.getRange()), Option(h.getKind()).map(_.getValue()).getOrElse(1))).toList
  }

  def hover(params: TextDocumentPositionParams): Option[Hover] = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val result = requirePc().hover(offsetParams(uri, params.position))
    result.toScala.map { sig =>
      val h = sig.toLsp()
      val contents = h.getContents()
      val markup =
        if (contents == null) MarkupContent("plaintext", "")
        else if (contents.isRight()) { val mc = contents.getRight(); MarkupContent(mc.getKind(), mc.getValue()) }
        else MarkupContent("plaintext", String.valueOf(contents.getLeft()))
      Hover(markup)
    }
  }

  def signatureHelp(params: TextDocumentPositionParams): SignatureHelp = thisServer.synchronized {
    val uri = new URI(params.textDocument.uri)
    val result = requirePc().signatureHelp(offsetParams(uri, params.position))
    SignatureHelp(
      signatures = result.getSignatures().asScala.map { sig =>
        SignatureInformation(
          label = sig.getLabel(),
          documentation = Option(sig.getDocumentation()).map(eitherToMarkupContent),
          parameters = Option(sig.getParameters()).map(_.asScala.map { p =>
            ParameterInformation(eitherToString(p.getLabel()), Option(p.getDocumentation()).map(eitherToMarkupContent))
          }.toList).getOrElse(Nil)
        )
      }.toList,
      activeParameter = Option(result.getActiveParameter()).map(_.intValue).getOrElse(-1),
      activeSignature = Option(result.getActiveSignature()).map(_.intValue).getOrElse(-1)
    )
  }
}
