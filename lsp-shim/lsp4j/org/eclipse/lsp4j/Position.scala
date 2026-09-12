package org.eclipse.lsp4j

class Position() {
  private var _line: Int = 0
  private var _character: Int = 0

  def this(line: Int, character: Int) = {
    this()
    _line = line
    _character = character
  }

  def getLine(): Int = _line
  def setLine(line: Int): Unit = _line = line
  def getCharacter(): Int = _character
  def setCharacter(character: Int): Unit = _character = character
}
