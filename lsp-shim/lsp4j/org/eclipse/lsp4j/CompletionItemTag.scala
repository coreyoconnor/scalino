package org.eclipse.lsp4j

enum CompletionItemTag(value: Int) {
  case Deprecated extends CompletionItemTag(1)

  def getValue(): Int = value
}

object CompletionItemTag {
  def forValue(value: Int): CompletionItemTag = values.find(_.getValue() == value).get
}
