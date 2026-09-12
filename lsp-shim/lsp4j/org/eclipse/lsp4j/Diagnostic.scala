package org.eclipse.lsp4j

import java.util as ju
import org.eclipse.lsp4j.jsonrpc.messages.Either

class Diagnostic() {
  private var _range: Range = null
  private var _message: Either[String, MarkupContent] = null
  private var _severity: DiagnosticSeverity = null
  private var _source: String = null
  private var _code: java.lang.Integer = null
  private var _tags: ju.List[DiagnosticTag] = null
  private var _data: Object = null

  def this(range: Range, message: String) = {
    this()
    _range = range
    _message = Either.forLeft(message)
  }

  def this(range: Range, message: String, severity: DiagnosticSeverity, source: String) = {
    this(range, message)
    _severity = severity
    _source = source
  }

  def getRange(): Range = _range
  def setRange(range: Range): Unit = _range = range

  // Real lsp4j types `message` as `Either<String, MarkupContent>`; the
  // constructor/setter here only ever take plain `String` (matching
  // `DiagnosticProvider.scala`'s real usage), but the getter needs the real
  // Either shape since `PcLanguageServer.scala` (this shim's own consumer,
  // not presentation-compiler) reads it back.
  def getMessage(): Either[String, MarkupContent] = _message
  def setMessage(message: String): Unit = _message = Either.forLeft(message)
  def getSeverity(): DiagnosticSeverity = _severity
  def setSeverity(severity: DiagnosticSeverity): Unit = _severity = severity
  def getSource(): String = _source
  def setSource(source: String): Unit = _source = source

  // Real lsp4j types `code` as `Either<String, Integer>`; only the plain-Int
  // overload is ever called (`DiagnosticProvider.scala`), so that's all this
  // shim exposes.
  def getCode(): java.lang.Integer = _code
  def setCode(code: java.lang.Integer): Unit = _code = code

  def getTags(): ju.List[DiagnosticTag] = _tags
  def setTags(tags: ju.List[DiagnosticTag]): Unit = _tags = tags
  def getData(): Object = _data
  def setData(data: Object): Unit = _data = data
}
