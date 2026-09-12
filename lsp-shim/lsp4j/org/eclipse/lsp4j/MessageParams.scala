package org.eclipse.lsp4j

class MessageParams {
  private var _type: MessageType = null
  private var _message: String = null

  def this(tpe: MessageType, message: String) = {
    this()
    _type = tpe
    _message = message
  }

  def getType(): MessageType = _type
  def setType(tpe: MessageType): Unit = _type = tpe
  def getMessage(): String = _message
  def setMessage(message: String): Unit = _message = message
}
