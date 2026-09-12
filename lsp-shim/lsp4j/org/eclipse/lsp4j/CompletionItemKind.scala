package org.eclipse.lsp4j

enum CompletionItemKind(value: Int) {
  case Text extends CompletionItemKind(1)
  case Method extends CompletionItemKind(2)
  case Function extends CompletionItemKind(3)
  case Constructor extends CompletionItemKind(4)
  case Field extends CompletionItemKind(5)
  case Variable extends CompletionItemKind(6)
  case Class extends CompletionItemKind(7)
  case Interface extends CompletionItemKind(8)
  case Module extends CompletionItemKind(9)
  case Property extends CompletionItemKind(10)
  case Unit extends CompletionItemKind(11)
  case Value extends CompletionItemKind(12)
  case Enum extends CompletionItemKind(13)
  case Keyword extends CompletionItemKind(14)
  case Snippet extends CompletionItemKind(15)
  case Color extends CompletionItemKind(16)
  case File extends CompletionItemKind(17)
  case Reference extends CompletionItemKind(18)
  case Folder extends CompletionItemKind(19)
  case EnumMember extends CompletionItemKind(20)
  case Constant extends CompletionItemKind(21)
  case Struct extends CompletionItemKind(22)
  case Event extends CompletionItemKind(23)
  case Operator extends CompletionItemKind(24)
  case TypeParameter extends CompletionItemKind(25)

  def getValue(): Int = value
}

object CompletionItemKind {
  def forValue(value: Int): CompletionItemKind = values.find(_.getValue() == value).get
}
