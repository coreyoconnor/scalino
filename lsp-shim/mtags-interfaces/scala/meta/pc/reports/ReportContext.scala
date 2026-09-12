package scala.meta.pc.reports

trait ReportContext {
  def unsanitized(): Reporter
  def incognito(): Reporter
  def bloop(): Reporter
}
