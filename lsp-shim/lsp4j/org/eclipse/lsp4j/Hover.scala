package org.eclipse.lsp4j

import org.eclipse.lsp4j.jsonrpc.messages.Either

class Hover() {
  private var _contents: Either[AnyRef, MarkupContent] = null
  private var _range: Range = null

  def this(contents: MarkupContent) = {
    this()
    _contents = Either.forRight(contents)
  }

  def this(contents: MarkupContent, range: Range) = {
    this()
    _contents = Either.forRight(contents)
    _range = range
  }

  // Real lsp4j types `contents` as
  // `Either<List<Either<String, MarkedString>>, MarkupContent>`; the
  // constructors here only ever take plain `MarkupContent` (matching real
  // presentation-compiler usage, `ScalaHover.toLsp()`), but the getter needs
  // the real Either shape since `PcLanguageServer.scala` (this shim's own
  // consumer) reads it back. `AnyRef` stands in for the real
  // `List<Either<String,MarkedString>>` left-branch type, never actually
  // constructed here.
  def getContents(): Either[AnyRef, MarkupContent] = _contents
  def setContents(contents: MarkupContent): Unit = _contents = Either.forRight(contents)
  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range
}
