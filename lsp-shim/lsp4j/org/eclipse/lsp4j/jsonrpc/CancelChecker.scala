package org.eclipse.lsp4j.jsonrpc

trait CancelChecker {
  def checkCanceled(): Unit
  def isCanceled(): Boolean = false
}
