package org.eclipse.lsp4j

import java.util as ju

class Command() {
  private var _title: String = null
  private var _command: String = null
  private var _arguments: ju.List[Object] = null

  def this(title: String, command: String) = {
    this()
    _title = title
    _command = command
  }

  def this(title: String, command: String, arguments: ju.List[Object]) = {
    this(title, command)
    _arguments = arguments
  }

  def getTitle(): String = _title
  def setTitle(title: String): Unit = _title = title
  def getCommand(): String = _command
  def setCommand(command: String): Unit = _command = command
  def getArguments(): ju.List[Object] = _arguments
  def setArguments(arguments: ju.List[Object]): Unit = _arguments = arguments
}
