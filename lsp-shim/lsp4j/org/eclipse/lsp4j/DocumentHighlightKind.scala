package org.eclipse.lsp4j

enum DocumentHighlightKind(value: Int) {
  case Text extends DocumentHighlightKind(1)
  case Read extends DocumentHighlightKind(2)
  case Write extends DocumentHighlightKind(3)

  def getValue(): Int = value
}

object DocumentHighlightKind {
  def forValue(value: Int): DocumentHighlightKind = values.find(_.getValue() == value).get
}
