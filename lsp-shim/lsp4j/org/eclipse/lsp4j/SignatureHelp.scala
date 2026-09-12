package org.eclipse.lsp4j

import java.util as ju

class SignatureHelp() {
  private var _signatures: ju.List[SignatureInformation] = null
  private var _activeSignature: java.lang.Integer = null
  private var _activeParameter: java.lang.Integer = null

  def this(signatures: ju.List[SignatureInformation], activeSignature: java.lang.Integer, activeParameter: java.lang.Integer) = {
    this()
    _signatures = signatures
    _activeSignature = activeSignature
    _activeParameter = activeParameter
  }

  def getSignatures(): ju.List[SignatureInformation] = _signatures
  def setSignatures(signatures: ju.List[SignatureInformation]): Unit = _signatures = signatures
  def getActiveSignature(): java.lang.Integer = _activeSignature
  def setActiveSignature(activeSignature: java.lang.Integer): Unit = _activeSignature = activeSignature
  def getActiveParameter(): java.lang.Integer = _activeParameter
  def setActiveParameter(activeParameter: java.lang.Integer): Unit = _activeParameter = activeParameter
}
