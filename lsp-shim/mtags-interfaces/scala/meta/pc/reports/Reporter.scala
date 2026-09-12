package scala.meta.pc.reports

import java.util.Optional
import java.nio.file.Path
import java.util.function.Supplier

trait Reporter {
  def create(report: Supplier[Report], ifVerbose: java.lang.Boolean): Optional[Path]
  def create(report: Supplier[Report]): Optional[Path] = create(report, java.lang.Boolean.FALSE)
}
