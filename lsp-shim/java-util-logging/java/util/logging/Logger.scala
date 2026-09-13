package java.util.logging

/** Minimal shim covering exactly the 2 `log` overloads presentation-compiler/
 *  mtags-shared call (`CompilerSearchVisitor.scala`, `CompilerAccess.scala`,
 *  `PackageIndex.scala`) -- `java.util.logging` has no Scala Native
 *  cross-build/javalib port. Writes to stderr rather than no-op, so these
 *  genuine error/warning logs (compiler crashes, classpath scan failures)
 *  stay visible for debugging this prototype, matching this project's own
 *  stderr-logging convention (see Main.scala's `Log`).
 */
final class Logger private (name: String) {
  def log(level: Level, msg: String): Unit =
    System.err.println(s"[$level] $name: $msg")
  def log(level: Level, msg: String, thrown: Throwable): Unit = {
    System.err.println(s"[$level] $name: $msg")
    if (thrown != null) thrown.printStackTrace()
  }
}
object Logger {
  def getLogger(name: String): Logger = new Logger(name)
}
