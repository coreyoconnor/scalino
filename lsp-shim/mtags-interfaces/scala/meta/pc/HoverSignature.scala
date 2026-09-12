package scala.meta.pc

import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.Hover

import java.util.Optional

trait HoverSignature {
  def toLsp(): Hover
  def signature(): Optional[String]
  def getRange(): Optional[Range]
  def withRange(range: Range): HoverSignature
  def contentType(): ContentType = ContentType.MARKDOWN
}
