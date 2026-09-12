package scala.meta.pc.reports

import java.nio.file.Path
import java.util.Optional
import java.util.function.Supplier

class EmptyReporter extends Reporter {
  override def create(report: Supplier[Report], ifVerbose: java.lang.Boolean): Optional[Path] =
    Optional.empty()
}
