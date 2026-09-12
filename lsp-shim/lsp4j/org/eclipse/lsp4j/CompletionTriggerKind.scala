package org.eclipse.lsp4j

enum CompletionTriggerKind(value: Int) {
  case Invoked extends CompletionTriggerKind(1)
  case TriggerCharacter extends CompletionTriggerKind(2)
  case TriggerForIncompleteCompletions extends CompletionTriggerKind(3)

  def getValue(): Int = value
}

object CompletionTriggerKind {
  def forValue(value: Int): CompletionTriggerKind = values.find(_.getValue() == value).get
}
