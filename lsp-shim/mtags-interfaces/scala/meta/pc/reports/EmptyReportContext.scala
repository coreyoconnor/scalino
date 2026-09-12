package scala.meta.pc.reports

class EmptyReportContext extends ReportContext {
  private val emptyReporter: Reporter = new EmptyReporter()
  override def unsanitized(): Reporter = emptyReporter
  override def incognito(): Reporter = emptyReporter
  override def bloop(): Reporter = emptyReporter
}
