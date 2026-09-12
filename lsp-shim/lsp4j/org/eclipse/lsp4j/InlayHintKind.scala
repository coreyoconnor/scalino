package org.eclipse.lsp4j

enum InlayHintKind(value: Int) {
  case Type extends InlayHintKind(1)
  case Parameter extends InlayHintKind(2)

  def getValue(): Int = value
}

object InlayHintKind {
  def forValue(value: Int): InlayHintKind = values.find(_.getValue() == value).get
}
