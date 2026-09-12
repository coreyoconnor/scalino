package org.eclipse.lsp4j.jsonrpc.messages

// Real lsp4j 1.0.0 shape (`javap`-verified): a protected two-slot union with
// static `forLeft`/`forRight` factories. Presentation-compiler's own source
// never touches this type (confirmed via grep — every real call site there
// uses Scala's own `scala.util.Either`) but the ported `mtags-interfaces`
// module's `ReferencesRequest`/`offsetOrSymbol()` does (`PcReferencesProvider
// .scala:28,32,37` calls `.isLeft()`/`.getLeft()`/`.getRight()` on it), so
// this needs real left/right semantics, not just a compile-time stub.
class Either[L, R] protected (left: L, right: R) {
  def getLeft(): L = left
  def getRight(): R = right
  def get(): Object = if (left != null) left.asInstanceOf[Object] else right.asInstanceOf[Object]
  def isLeft(): Boolean = left != null
  def isRight(): Boolean = right != null

  def map[T](mapLeft: java.util.function.Function[? >: L, ? <: T], mapRight: java.util.function.Function[? >: R, ? <: T]): T =
    if (isLeft()) mapLeft.apply(left) else mapRight.apply(right)
}

object Either {
  def forLeft[L, R](left: L): Either[L, R] = new Either(left, null.asInstanceOf[R])
  def forRight[L, R](right: R): Either[L, R] = new Either(null.asInstanceOf[L], right)
}
