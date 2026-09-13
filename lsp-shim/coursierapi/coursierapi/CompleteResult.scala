package coursierapi

final class CompleteResult private (from: Int, completions: java.util.List[String]) {
  def getFrom(): Int = from
  def getCompletions(): java.util.List[String] = completions
}
object CompleteResult {
  def of(from: Int, completions: java.util.List[String]): CompleteResult = new CompleteResult(from, completions)
}
