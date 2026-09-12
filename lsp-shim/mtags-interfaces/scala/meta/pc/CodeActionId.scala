package scala.meta.pc

/** All known code action Ids, this is not a complete list, new code actions might
 * be added by clients, which is why this is not an enum.
 */
object CodeActionId {
  val ConvertToNamedArguments = "ConvertToNamedArguments"
  val ExtractMethod = "ExtractMethod"
  val ImplementAbstractMembers = "ImplementAbstractMembers"
  val ImportMissingSymbol = "ImportMissingSymbol"
  val InlineValue = "InlineValue"
  val InsertInferredType = "InsertInferredType"
  val InsertInferredMethod = "InsertInferredMethod"
  val ConvertToNamedLambdaParameters = "ConvertToNamedLambdaParameters"
}
