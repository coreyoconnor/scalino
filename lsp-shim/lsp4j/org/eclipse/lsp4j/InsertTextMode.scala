package org.eclipse.lsp4j

enum InsertTextMode(value: Int) {
  case AsIs extends InsertTextMode(1)
  case AdjustIndentation extends InsertTextMode(2)

  def getValue(): Int = value
}

object InsertTextMode {
  def forValue(value: Int): InsertTextMode = values.find(_.getValue() == value).get
}
