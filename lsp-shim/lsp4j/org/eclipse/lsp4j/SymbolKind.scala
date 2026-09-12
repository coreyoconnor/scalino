package org.eclipse.lsp4j

enum SymbolKind(value: Int) {
  case File extends SymbolKind(1)
  case Module extends SymbolKind(2)
  case Namespace extends SymbolKind(3)
  case Package extends SymbolKind(4)
  case Class extends SymbolKind(5)
  case Method extends SymbolKind(6)
  case Property extends SymbolKind(7)
  case Field extends SymbolKind(8)
  case Constructor extends SymbolKind(9)
  case Enum extends SymbolKind(10)
  case Interface extends SymbolKind(11)
  case Function extends SymbolKind(12)
  case Variable extends SymbolKind(13)
  case Constant extends SymbolKind(14)
  case String extends SymbolKind(15)
  case Number extends SymbolKind(16)
  case Boolean extends SymbolKind(17)
  case Array extends SymbolKind(18)
  case Object extends SymbolKind(19)
  case Key extends SymbolKind(20)
  case Null extends SymbolKind(21)
  case EnumMember extends SymbolKind(22)
  case Struct extends SymbolKind(23)
  case Event extends SymbolKind(24)
  case Operator extends SymbolKind(25)
  case TypeParameter extends SymbolKind(26)

  def getValue(): Int = value
}

object SymbolKind {
  def forValue(value: Int): SymbolKind = values.find(_.getValue() == value).get
}
