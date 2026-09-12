package org.eclipse.lsp4j

enum InsertTextFormat(value: Int) {
  case PlainText extends InsertTextFormat(1)
  case Snippet extends InsertTextFormat(2)

  def getValue(): Int = value
}

object InsertTextFormat {
  def forValue(value: Int): InsertTextFormat = values.find(_.getValue() == value).get
}
