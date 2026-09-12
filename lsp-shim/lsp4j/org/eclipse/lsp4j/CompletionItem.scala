package org.eclipse.lsp4j

import java.util as ju
import org.eclipse.lsp4j.jsonrpc.messages.Either

class CompletionItem() {
  private var _label: String = null
  private var _kind: CompletionItemKind = null
  private var _tags: ju.List[CompletionItemTag] = null
  private var _detail: String = null
  private var _documentation: Either[String, MarkupContent] = null
  private var _deprecated: java.lang.Boolean = null
  private var _sortText: String = null
  private var _filterText: String = null
  private var _insertText: String = null
  private var _insertTextFormat: InsertTextFormat = null
  private var _insertTextMode: InsertTextMode = null
  private var _textEdit: TextEdit = null
  private var _additionalTextEdits: ju.List[TextEdit] = null
  private var _command: Command = null
  private var _data: Object = null

  def this(label: String) = {
    this()
    _label = label
  }

  def getLabel(): String = _label
  def setLabel(label: String): Unit = _label = label
  def getKind(): CompletionItemKind = _kind
  def setKind(kind: CompletionItemKind): Unit = _kind = kind
  def getTags(): ju.List[CompletionItemTag] = _tags
  def setTags(tags: ju.List[CompletionItemTag]): Unit = _tags = tags
  def getDetail(): String = _detail
  def setDetail(detail: String): Unit = _detail = detail

  // Real lsp4j types `documentation` as `Either<String, MarkupContent>`; the
  // setter here only ever takes plain `MarkupContent` (matching real
  // presentation-compiler usage), but the getter needs the real Either
  // shape since `PcLanguageServer.scala` (this shim's own consumer) reads
  // it back.
  def getDocumentation(): Either[String, MarkupContent] = _documentation
  def setDocumentation(documentation: MarkupContent): Unit = _documentation = Either.forRight(documentation)

  def getDeprecated(): java.lang.Boolean = _deprecated
  def setDeprecated(deprecated: java.lang.Boolean): Unit = _deprecated = deprecated
  def getSortText(): String = _sortText
  def setSortText(sortText: String): Unit = _sortText = sortText
  def getFilterText(): String = _filterText
  def setFilterText(filterText: String): Unit = _filterText = filterText
  def getInsertText(): String = _insertText
  def setInsertText(insertText: String): Unit = _insertText = insertText
  def getInsertTextFormat(): InsertTextFormat = _insertTextFormat
  def setInsertTextFormat(insertTextFormat: InsertTextFormat): Unit = _insertTextFormat = insertTextFormat
  def getInsertTextMode(): InsertTextMode = _insertTextMode
  def setInsertTextMode(insertTextMode: InsertTextMode): Unit = _insertTextMode = insertTextMode

  // Real lsp4j types `textEdit` as `Either<TextEdit, InsertReplaceEdit>`; only
  // the plain-`TextEdit` shape is ever constructed by presentation-compiler
  // (`CompletionProvider.scala`), so that's all this shim exposes.
  def getTextEdit(): TextEdit = _textEdit
  def setTextEdit(textEdit: TextEdit): Unit = _textEdit = textEdit

  def getAdditionalTextEdits(): ju.List[TextEdit] = _additionalTextEdits
  def setAdditionalTextEdits(additionalTextEdits: ju.List[TextEdit]): Unit = _additionalTextEdits = additionalTextEdits
  def getCommand(): Command = _command
  def setCommand(command: Command): Unit = _command = command
  def getData(): Object = _data
  def setData(data: Object): Unit = _data = data
}
