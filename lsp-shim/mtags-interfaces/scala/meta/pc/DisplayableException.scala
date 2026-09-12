package scala.meta.pc

/** An exception whose message is to be displayed to the user.
 * Currently used when a code action command cannot be executed
 * but the appropriate condition could not be checked when creating the action.
 */
class DisplayableException(message: String) extends RuntimeException(message)
