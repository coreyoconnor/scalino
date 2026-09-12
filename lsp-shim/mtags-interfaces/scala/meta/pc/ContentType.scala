package scala.meta.pc

enum ContentType(private val label: String):
  case MARKDOWN extends ContentType("markdown")
  case PLAINTEXT extends ContentType("plaintext")

  override def toString(): String = label
