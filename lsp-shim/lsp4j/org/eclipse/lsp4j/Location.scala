package org.eclipse.lsp4j

class Location() {
  private var _uri: String = null
  private var _range: Range = null

  def this(uri: String, range: Range) = {
    this()
    _uri = uri
    _range = range
  }

  def getUri(): String = _uri
  def setUri(uri: String): Unit = _uri = uri
  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range
}
