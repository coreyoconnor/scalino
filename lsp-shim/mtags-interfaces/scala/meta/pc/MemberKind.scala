package scala.meta.pc

enum MemberKind(private val value: String):
  case TOPLEVEL_TYPE extends MemberKind("ToplevelType")
  case TOPLEVEL_IMPLICIT_CLASS extends MemberKind("ToplevelImplicitClass")

object MemberKind {
  val ALL: java.util.List[MemberKind] =
    java.util.Arrays.asList(MemberKind.TOPLEVEL_TYPE, MemberKind.TOPLEVEL_IMPLICIT_CLASS)
}
