package org.eclipse.lsp4j

enum DiagnosticTag(value: Int) {
  case Unnecessary extends DiagnosticTag(1)
  case Deprecated extends DiagnosticTag(2)

  def getValue(): Int = value
}

object DiagnosticTag {
  def forValue(value: Int): DiagnosticTag = values.find(_.getValue() == value).get
}
