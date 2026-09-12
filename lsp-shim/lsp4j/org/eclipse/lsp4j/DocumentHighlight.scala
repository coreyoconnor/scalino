package org.eclipse.lsp4j

class DocumentHighlight() {
  private var _range: Range = null
  private var _kind: DocumentHighlightKind = null

  def this(range: Range) = {
    this()
    _range = range
  }

  def this(range: Range, kind: DocumentHighlightKind) = {
    this(range)
    _kind = kind
  }

  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range
  def getKind(): DocumentHighlightKind = _kind
  def setKind(kind: DocumentHighlightKind): Unit = _kind = kind
}
