package scala.meta.internal.metals

import scala.language.unsafeNulls
import java.net.URI
import java.util.Optional

import scala.meta.pc.CancelToken
import scala.meta.pc.OutlineFiles
import scala.meta.pc.VirtualFileParams

case class CompilerVirtualFileParams(
    uri: URI,
    text: String,
    token: CancelToken,
    override val outlineFiles: Optional[OutlineFiles]
) extends VirtualFileParams {
  // Real upstream default (VirtualFileParams.shouldReturnDiagnostics()) is
  // false -- real Metals callers opt in per call site (e.g.
  // Compilers.scala's on-type-diagnostics path) because Metals' main
  // diagnostics stream comes from BSP/bloop compilation, not the
  // presentation compiler. This self-hosted LSP has no separate build
  // server: didChange/didOpen's diagnostics ARE the only diagnostics
  // source, and CompilerVirtualFileParams is this shim's only VirtualFileParams
  // implementation (PcLanguageServer.scala's didChange/didOpen), so always
  // returning true here is correct for every real caller in this build.
  override def shouldReturnDiagnostics(): Boolean = true
}

object CompilerVirtualFileParams {
  def apply(
      uri: URI,
      text: String,
      token: CancelToken = EmptyCancelToken
  ): CompilerVirtualFileParams =
    CompilerVirtualFileParams(uri, text, token, Optional.empty())
}
