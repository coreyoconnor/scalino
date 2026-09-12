package org.eclipse.lsp4j

class Range() {
  private var _start: Position = null
  private var _end: Position = null

  def this(start: Position, end: Position) = {
    this()
    _start = start
    _end = end
  }

  def getStart(): Position = _start
  def setStart(start: Position): Unit = _start = start
  def getEnd(): Position = _end
  def setEnd(end: Position): Unit = _end = end
}
