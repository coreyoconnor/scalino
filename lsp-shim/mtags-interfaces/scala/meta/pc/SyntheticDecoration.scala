package scala.meta.pc

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.InlayHintLabelPart

trait SyntheticDecoration {
  def range(): Range
  def label(): String
  def kind(): Int
}
