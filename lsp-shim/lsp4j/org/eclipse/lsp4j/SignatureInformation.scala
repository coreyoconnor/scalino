package org.eclipse.lsp4j

import java.util as ju
import org.eclipse.lsp4j.jsonrpc.messages.Either

class SignatureInformation() {
  private var _label: String = null
  private var _documentation: Either[String, MarkupContent] = null
  private var _parameters: ju.List[ParameterInformation] = null

  def this(label: String) = {
    this()
    _label = label
  }

  def getLabel(): String = _label
  def setLabel(label: String): Unit = _label = label

  // Real lsp4j types `documentation` as `Either<String, MarkupContent>`; the
  // setter here only ever takes plain `MarkupContent` (matching real
  // presentation-compiler usage), but the getter needs the real Either
  // shape since `PcLanguageServer.scala` (this shim's own consumer) reads
  // it back.
  def getDocumentation(): Either[String, MarkupContent] = _documentation
  def setDocumentation(documentation: MarkupContent): Unit = _documentation = Either.forRight(documentation)

  def getParameters(): ju.List[ParameterInformation] = _parameters
  def setParameters(parameters: ju.List[ParameterInformation]): Unit = _parameters = parameters
}
