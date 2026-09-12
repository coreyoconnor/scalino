package org.eclipse.lsp4j

enum MessageType(value: Int) {
  case Error extends MessageType(1)
  case Warning extends MessageType(2)
  case Info extends MessageType(3)
  case Log extends MessageType(4)
  case Debug extends MessageType(5)

  def getValue(): Int = value
}

object MessageType {
  def forValue(value: Int): MessageType = values.find(_.getValue() == value).get
}
