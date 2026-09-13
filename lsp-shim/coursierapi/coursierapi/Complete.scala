package coursierapi

/** Stub for real `io.get-coursier:interface`'s `coursierapi.Complete`
 *  (Ammonite/ScalaCli-style `import $ivy"..."` dependency-string
 *  completions) -- no Scala Native cross-build exists for that jar, and
 *  this is a narrow, rarely-triggered completion feature (see
 *  `scala.meta.internal.mtags.CoursierComplete`, this shim's only caller).
 *  Real-network dependency resolution isn't implemented here: `complete()`
 *  always returns an empty result, same as a real lookup that found no
 *  matches -- no functionality loss beyond this one completion trigger.
 */
final class Complete private () {
  def withScalaVersion(v: String): Complete = this
  def withScalaBinaryVersion(v: String): Complete = this
  def withInput(s: String): Complete = this
  def complete(): CompleteResult = CompleteResult.of(0, new java.util.ArrayList[String]())
}
object Complete {
  def create(): Complete = new Complete()
}
