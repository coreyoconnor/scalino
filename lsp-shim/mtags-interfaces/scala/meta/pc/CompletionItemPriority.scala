package scala.meta.pc

trait CompletionItemPriority {
  /** Returns an integer that is used to sort workspaceMembers with the same name.
   * A lower number indicates a higher priority.
   */
  def workspaceMemberPriority(symbol: String): Integer
}
