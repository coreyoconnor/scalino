package org.eclipse.lsp4j

class MarkupContent() {
  private var _kind: String = null
  private var _value: String = null

  def this(kind: String, value: String) = {
    this()
    _kind = kind
    _value = value
  }

  def getKind(): String = _kind
  def setKind(kind: String): Unit = _kind = kind
  def getValue(): String = _value
  def setValue(value: String): Unit = _value = value
}
