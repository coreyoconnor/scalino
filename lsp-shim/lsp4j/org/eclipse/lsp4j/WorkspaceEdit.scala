package org.eclipse.lsp4j

import java.util as ju

class WorkspaceEdit() {
  private var _changes: ju.Map[String, ju.List[TextEdit]] = null

  def this(changes: ju.Map[String, ju.List[TextEdit]]) = {
    this()
    _changes = changes
  }

  def getChanges(): ju.Map[String, ju.List[TextEdit]] = _changes
  def setChanges(changes: ju.Map[String, ju.List[TextEdit]]): Unit = _changes = changes
}
