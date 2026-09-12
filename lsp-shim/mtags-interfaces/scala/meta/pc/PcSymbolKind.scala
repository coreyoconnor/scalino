package scala.meta.pc

enum PcSymbolKind(private val value: Int):
  case UNKNOWN_KIND extends PcSymbolKind(0)
  case METHOD extends PcSymbolKind(3)
  case MACRO extends PcSymbolKind(6)
  case TYPE extends PcSymbolKind(7)
  case PARAMETER extends PcSymbolKind(8)
  case TYPE_PARAMETER extends PcSymbolKind(9)
  case OBJECT extends PcSymbolKind(10)
  case PACKAGE extends PcSymbolKind(11)
  case PACKAGE_OBJECT extends PcSymbolKind(12)
  case CLASS extends PcSymbolKind(13)
  case TRAIT extends PcSymbolKind(14)
  case SELF_PARAMETER extends PcSymbolKind(17)
  case INTERFACE extends PcSymbolKind(18)
  case LOCAL extends PcSymbolKind(19)
  case FIELD extends PcSymbolKind(20)
  case CONSTRUCTOR extends PcSymbolKind(21)

  def getValue(): Int = value
