package scala.meta.internal.pc

import scala.language.unsafeNulls

case class CompletionItemData(
    symbol: String,
    target: String,
    // The kind of the completion item, for example `override def`
    kind: java.lang.Integer = null,
    additionalSymbols: java.util.List[String] = null
) {

  /** Hand-rolled JSON encoding (no Gson/reflection -- consistent with this
   *  project's zero-reflection codec philosophy). Returns a plain `String`,
   *  suitable to store directly as the opaque `Object` in
   *  `org.eclipse.lsp4j.CompletionItem#data` -- see
   *  `CompletionItemData.fromJson` in `CommonMtagsEnrichments.scala` for the
   *  matching decode.
   */
  def toJson: String = {
    def jsonString(s: String): String = {
      val sb = new java.lang.StringBuilder(s.length + 2)
      sb.append('"')
      var i = 0
      while (i < s.length) {
        val c = s.charAt(i)
        c match {
          case '"' => sb.append("\\\"")
          case '\\' => sb.append("\\\\")
          case '\n' => sb.append("\\n")
          case '\r' => sb.append("\\r")
          case '\t' => sb.append("\\t")
          case c if c < 0x20 => sb.append("\\u%04x".format(c.toInt))
          case c => sb.append(c)
        }
        i += 1
      }
      sb.append('"')
      sb.toString
    }
    val sb = new java.lang.StringBuilder()
    sb.append('{')
    sb.append("\"symbol\":").append(jsonString(symbol))
    sb.append(",\"target\":").append(jsonString(target))
    if (kind != null) {
      sb.append(",\"kind\":").append(kind.intValue())
    }
    if (additionalSymbols != null) {
      sb.append(",\"additionalSymbols\":[")
      var first = true
      additionalSymbols.forEach { s =>
        if (!first) sb.append(',')
        sb.append(jsonString(s))
        first = false
      }
      sb.append(']')
    }
    sb.append('}')
    sb.toString
  }
}

object CompletionItemData {
  def empty: CompletionItemData = CompletionItemData("", "")
  val None: java.lang.Integer = 0
  // This is an `override def` completion item.
  val OverrideKind: java.lang.Integer = 1
  // This is a completion implementing all abstract members
  val ImplementAllKind: java.lang.Integer = 2

  /** Hand-rolled JSON decoding matching `toJson`'s exact output shape --
   *  deliberately minimal (this fixed shape only), not a general JSON
   *  parser.
   */
  def fromJson(json: String): Option[CompletionItemData] = {
    try {
      var i = 0
      val n = json.length

      def skipWs(): Unit = { while (i < n && json.charAt(i).isWhitespace) i += 1 }

      def expect(c: Char): Unit = {
        skipWs()
        if (i >= n || json.charAt(i) != c)
          throw new IllegalArgumentException(s"expected '$c' at $i in $json")
        i += 1
      }

      def parseString(): String = {
        skipWs()
        expect('"')
        val sb = new java.lang.StringBuilder()
        while (i < n && json.charAt(i) != '"') {
          val c = json.charAt(i)
          if (c == '\\' && i + 1 < n) {
            json.charAt(i + 1) match {
              case '"' => sb.append('"'); i += 2
              case '\\' => sb.append('\\'); i += 2
              case 'n' => sb.append('\n'); i += 2
              case 'r' => sb.append('\r'); i += 2
              case 't' => sb.append('\t'); i += 2
              case 'u' =>
                val hex = json.substring(i + 2, i + 6)
                sb.append(Integer.parseInt(hex, 16).toChar)
                i += 6
              case other => sb.append(other); i += 2
            }
          } else {
            sb.append(c)
            i += 1
          }
        }
        expect('"')
        sb.toString
      }

      def parseInt(): Int = {
        skipWs()
        val start = i
        while (i < n && (json.charAt(i).isDigit || json.charAt(i) == '-')) i += 1
        json.substring(start, i).toInt
      }

      var symbol = ""
      var target = ""
      var kind: java.lang.Integer = null
      var additionalSymbols: java.util.List[String] = null

      expect('{')
      skipWs()
      var continue = i < n && json.charAt(i) != '}'
      while (continue) {
        val key = parseString()
        expect(':')
        key match {
          case "symbol" => symbol = parseString()
          case "target" => target = parseString()
          case "kind" => kind = java.lang.Integer.valueOf(parseInt())
          case "additionalSymbols" =>
            expect('[')
            val list = new java.util.ArrayList[String]()
            skipWs()
            var more = i < n && json.charAt(i) != ']'
            while (more) {
              list.add(parseString())
              skipWs()
              if (i < n && json.charAt(i) == ',') { i += 1; more = true }
              else more = false
            }
            expect(']')
            additionalSymbols = list
          case _ =>
            // Unknown key -- skip its value (string or int only, this
            // shape never nests further).
            skipWs()
            if (i < n && json.charAt(i) == '"') parseString()
            else parseInt()
        }
        skipWs()
        if (i < n && json.charAt(i) == ',') { i += 1; continue = true }
        else continue = false
      }
      expect('}')

      Some(CompletionItemData(symbol, target, kind, additionalSymbols))
    } catch {
      case _: Exception => scala.None
    }
  }
}
