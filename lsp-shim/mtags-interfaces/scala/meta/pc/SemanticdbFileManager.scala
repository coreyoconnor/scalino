package scala.meta.pc

import java.nio.file.Path
import java.util as ju

trait SemanticdbFileManager {
  def listAllPackages(): ju.Map[String, ju.Set[Path]] = ju.Collections.emptyMap()
}

object SemanticdbFileManager {
  val EMPTY: SemanticdbFileManager = new SemanticdbFileManager {}
}
