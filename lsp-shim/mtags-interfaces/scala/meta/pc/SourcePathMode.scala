package scala.meta.pc

enum SourcePathMode {
  case DISABLED, FULL, PRUNED, MBT

  // Real mtags-interfaces has this method; unused anywhere in
  // presentation-compiler's own source (confirmed via grep), kept only for
  // API-shape completeness.
  def shouldPrune(): Boolean = this == SourcePathMode.PRUNED
}
