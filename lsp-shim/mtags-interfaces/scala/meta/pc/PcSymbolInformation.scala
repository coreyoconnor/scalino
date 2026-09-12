package scala.meta.pc

import java.util.Collections

trait PcSymbolInformation {
  def symbol(): String
  def kind(): PcSymbolKind
  def parents(): java.util.List[String]
  def dealiasedSymbol(): String
  def classOwner(): String
  def overriddenSymbols(): java.util.List[String]
  def alternativeSymbols(): java.util.List[String]
  def properties(): java.util.List[PcSymbolProperty]
  def recursiveParents(): java.util.List[String] = Collections.emptyList()
  def annotations(): java.util.List[String] = Collections.emptyList()
  def memberDefsAnnotations(): java.util.List[String] = Collections.emptyList()
  def typeParameters(): java.util.List[String] = Collections.emptyList()
}
