package org.eclipse.lsp4j

import org.eclipse.lsp4j.jsonrpc.messages.Either

class ParameterInformation() {
  private var _label: Either[String, AnyRef] = null
  private var _documentation: Either[String, MarkupContent] = null

  def this(label: String) = {
    this()
    _label = Either.forLeft(label)
  }

  // Real lsp4j types `label` as `Either<String, Tuple$Two<Integer, Integer>>`;
  // the constructor here only ever takes plain `String` (matching real
  // presentation-compiler usage), but the getter needs the real Either
  // shape since `PcLanguageServer.scala` (this shim's own consumer) reads
  // it back. `AnyRef` stands in for the real `Tuple$Two<Integer,Integer>`
  // right-branch type, never actually constructed here.
  def getLabel(): Either[String, AnyRef] = _label
  def setLabel(label: String): Unit = _label = Either.forLeft(label)

  // Real lsp4j types `documentation` as `Either<String, MarkupContent>`; the
  // setter here only ever takes plain `MarkupContent` (matching real
  // presentation-compiler usage), but the getter needs the real Either
  // shape since `PcLanguageServer.scala` (this shim's own consumer) reads
  // it back.
  def getDocumentation(): Either[String, MarkupContent] = _documentation
  def setDocumentation(documentation: MarkupContent): Unit = _documentation = Either.forRight(documentation)
}
