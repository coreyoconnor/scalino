package org.eclipse.lsp4j

object CodeActionKind {
  val Empty = ""
  val QuickFix = "quickfix"
  val Refactor = "refactor"
  val RefactorExtract = "refactor.extract"
  val RefactorInline = "refactor.inline"
  val RefactorMove = "refactor.move"
  val RefactorRewrite = "refactor.rewrite"
  val Source = "source"
  val SourceOrganizeImports = "source.organizeImports"
  val SourceFixAll = "source.fixAll"
  val Notebook = "notebook"
}
