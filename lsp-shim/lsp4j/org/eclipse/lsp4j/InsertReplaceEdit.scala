package org.eclipse.lsp4j

class InsertReplaceEdit() {
  private var _newText: String = null
  private var _insert: Range = null
  private var _replace: Range = null

  def this(newText: String, insert: Range, replace: Range) = {
    this()
    _newText = newText
    _insert = insert
    _replace = replace
  }

  def getNewText(): String = _newText
  def setNewText(newText: String): Unit = _newText = newText
  def getInsert(): Range = _insert
  def setInsert(insert: Range): Unit = _insert = insert
  def getReplace(): Range = _replace
  def setReplace(replace: Range): Unit = _replace = replace
}
