package org.eclipse.lsp4j

enum DiagnosticSeverity(value: Int) {
  case Error extends DiagnosticSeverity(1)
  case Warning extends DiagnosticSeverity(2)
  case Information extends DiagnosticSeverity(3)
  case Hint extends DiagnosticSeverity(4)

  def getValue(): Int = value
}

object DiagnosticSeverity {
  def forValue(value: Int): DiagnosticSeverity = values.find(_.getValue() == value).get
}
