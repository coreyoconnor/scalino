package scala.meta.pc.reports

import java.util.Optional
import java.net.URI

trait Report {
  def name(): String
  def shortSummary(): String
  def path(): Optional[URI]
  def id(): Optional[String]
  def fullText(withIdAndSummary: Boolean): String
}
