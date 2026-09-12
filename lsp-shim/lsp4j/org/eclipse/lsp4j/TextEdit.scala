package org.eclipse.lsp4j

class TextEdit() {
  private var _range: Range = null
  private var _newText: String = null

  def this(range: Range, newText: String) = {
    this()
    _range = range
    _newText = newText
  }

  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range
  def getNewText(): String = _newText
  def setNewText(newText: String): Unit = _newText = newText
}
