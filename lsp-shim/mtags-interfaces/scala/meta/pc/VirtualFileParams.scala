package scala.meta.pc

import java.net.URI
import java.util.Optional

/** Parameters for a presentation compiler request at a given offset in a single source file. */
trait VirtualFileParams {
  def uri(): URI
  def text(): String
  def token(): CancelToken

  def outlineFiles(): Optional[OutlineFiles] = Optional.empty()
  def shouldReturnDiagnostics(): Boolean = false
  def data(): Object = null
  def checkCanceled(): Unit = token().checkCanceled()
}
