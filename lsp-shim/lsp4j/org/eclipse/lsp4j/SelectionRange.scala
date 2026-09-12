package org.eclipse.lsp4j

class SelectionRange() {
  private var _range: Range = null
  private var _parent: SelectionRange = null

  def this(range: Range, parent: SelectionRange) = {
    this()
    _range = range
    _parent = parent
  }

  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range
  def getParent(): SelectionRange = _parent
  def setParent(parent: SelectionRange): Unit = _parent = parent
}
