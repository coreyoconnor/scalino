package org.eclipse.lsp4j

class CodeAction() {
  private var _title: String = null
  private var _kind: String = null
  private var _isPreferred: java.lang.Boolean = null
  private var _edit: WorkspaceEdit = null
  private var _command: Command = null
  private var _data: Object = null

  def this(title: String) = {
    this()
    _title = title
  }

  def getTitle(): String = _title
  def setTitle(title: String): Unit = _title = title
  def getKind(): String = _kind
  def setKind(kind: String): Unit = _kind = kind
  def getIsPreferred(): java.lang.Boolean = _isPreferred
  def setIsPreferred(isPreferred: java.lang.Boolean): Unit = _isPreferred = isPreferred
  def getEdit(): WorkspaceEdit = _edit
  def setEdit(edit: WorkspaceEdit): Unit = _edit = edit
  def getCommand(): Command = _command
  def setCommand(command: Command): Unit = _command = command
  def getData(): Object = _data
  def setData(data: Object): Unit = _data = data
}
