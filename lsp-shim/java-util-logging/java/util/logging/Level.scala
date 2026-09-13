package java.util.logging

/** Minimal shim for the 3 constants presentation-compiler/mtags-shared
 *  actually reference (INFO/WARNING/SEVERE) -- `java.util.logging` has no
 *  Scala Native cross-build/javalib port, and porting the real package is
 *  out of scope for what these 3 call sites need. See
 *  lsp-shim/java-util-logging/java/util/logging/Logger.scala.
 */
final class Level private (name: String) {
  override def toString(): String = name
}
object Level {
  val SEVERE: Level = new Level("SEVERE")
  val WARNING: Level = new Level("WARNING")
  val INFO: Level = new Level("INFO")
}
