package org.eclipse.lsp4j

import java.util as ju

// Real lsp4j 1.0.0 types the `label`/`tooltip` fields to
// `Either<String, List<InlayHintLabelPart>>` / `Either<String, MarkupContent>`.
// No call site anywhere in presentation-compiler or the ported mtags-shared
// source needs the list/MarkupContent variant, so this shim only exposes the
// plain-String convenience shape (mirrors the real jar's own `setLabel(String)`
// / `setTooltip(String)` overloads without also needing to model `Either`).
class InlayHint() {
  private var _position: Position = null
  private var _label: String = null
  private var _kind: InlayHintKind = null
  private var _textEdits: ju.List[TextEdit] = null
  private var _tooltip: String = null
  private var _paddingLeft: java.lang.Boolean = null
  private var _paddingRight: java.lang.Boolean = null
  private var _data: Object = null

  def this(position: Position, label: String) = {
    this()
    _position = position
    _label = label
  }

  def getPosition(): Position = _position
  def setPosition(position: Position): Unit = _position = position
  def getLabel(): String = _label
  def setLabel(label: String): Unit = _label = label
  def getKind(): InlayHintKind = _kind
  def setKind(kind: InlayHintKind): Unit = _kind = kind
  def getTextEdits(): ju.List[TextEdit] = _textEdits
  def setTextEdits(textEdits: ju.List[TextEdit]): Unit = _textEdits = textEdits
  def getTooltip(): String = _tooltip
  def setTooltip(tooltip: String): Unit = _tooltip = tooltip
  def getPaddingLeft(): java.lang.Boolean = _paddingLeft
  def setPaddingLeft(paddingLeft: java.lang.Boolean): Unit = _paddingLeft = paddingLeft
  def getPaddingRight(): java.lang.Boolean = _paddingRight
  def setPaddingRight(paddingRight: java.lang.Boolean): Unit = _paddingRight = paddingRight
  def getData(): Object = _data
  def setData(data: Object): Unit = _data = data
}
