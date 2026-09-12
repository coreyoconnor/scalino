package org.eclipse.lsp4j

import java.util as ju

class CompletionList() {
  private var _isIncomplete: Boolean = false
  private var _items: ju.List[CompletionItem] = null

  def this(items: ju.List[CompletionItem]) = {
    this()
    _items = items
  }

  def this(isIncomplete: Boolean, items: ju.List[CompletionItem]) = {
    this()
    _isIncomplete = isIncomplete
    _items = items
  }

  def isIncomplete(): Boolean = _isIncomplete
  def setIsIncomplete(isIncomplete: Boolean): Unit = _isIncomplete = isIncomplete
  def getItems(): ju.List[CompletionItem] = _items
  def setItems(items: ju.List[CompletionItem]): Unit = _items = items
}
