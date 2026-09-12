package scala.meta.internal.pc

import scala.language.unsafeNulls
import java.net.URI

import scala.collection.mutable.ListBuffer

import scala.meta.internal.jdk.CollectionConverters._
import scala.meta.internal.mtags.CommonMtagsEnrichments.XtensionText

import org.eclipse.lsp4j.InlayHint
import org.eclipse.lsp4j.InlayHintKind
import org.eclipse.{lsp4j => l}

sealed trait InlayHintOrigin
object InlayHintOrigin {
  case object NamedParameters extends InlayHintOrigin
  case object ByNameParameters extends InlayHintOrigin
  case object ImplicitConversion extends InlayHintOrigin
  case object ImplicitParameters extends InlayHintOrigin
  case object TypeParameters extends InlayHintOrigin
  case object InferredType extends InlayHintOrigin
  case object ClosingLabel extends InlayHintOrigin
  case object XRayMode extends InlayHintOrigin
}

case class InlayHintWithOrigin(
    hint: InlayHint,
    origin: InlayHintOrigin
)

case class InlayHints(
    uri: URI,
    inlayHints: List[InlayHintWithOrigin],
    blockInlayHints: Map[Int, InlayHintBlock]
) {

  def add(
      inlayHint: InlayHint,
      origin: InlayHintOrigin
  ): InlayHints = copy(inlayHints = addInlayHint(inlayHint, origin))

  def add(
      pos: l.Range,
      labelParts: List[LabelPart],
      kind: InlayHintKind,
      origin: InlayHintOrigin
  ): InlayHints =
    copy(inlayHints =
      addInlayHint(
        InlayHints.makeInlayHint(pos.getStart(), labelParts, kind, uri),
        origin
      )
    )

  /*
   * Collects inlay hints in a single expression together and aligns their type labels.
   * Note: Designed for use somewhat specifically for Xray Mode,
   * so it includes some filtering logic specific to that use case.
   */
  def addToBlock(
      pos: l.Range,
      labelParts: List[LabelPart],
      kind: InlayHintKind
  ): InlayHints = {
    // The start pos of each element in a single expression will be the beginning of the expression.
    // We can use this to associate related hints in a map
    val expressionStart = pos.getStart.getLine

    blockInlayHints
      .get(expressionStart)
      .fold(
        copy(blockInlayHints =
          blockInlayHints + (
            pos.getStart.getLine ->
              InlayHintBlock(
                indentLevel = pos.getEnd.getCharacter,
                List(
                  BlockInlayHint(
                    pos,
                    labelParts,
                    kind
                  )
                )
              )
          )
        )
      ) { (ihb: InlayHintBlock) =>
        val newLevel = math.max(pos.getEnd.getCharacter, ihb.indentLevel)

        val newBlock =
          InlayHintBlock(
            indentLevel = newLevel,
            ihb.hints :+ BlockInlayHint(pos, labelParts, kind)
          )

        copy(blockInlayHints = blockInlayHints + (expressionStart -> newBlock))
      }
  }

  private def makeInlayHint(
      bih: BlockInlayHint
  ): InlayHint =
    InlayHints.makeInlayHint(bih.pos.getEnd, bih.labels, bih.kind, uri)

  /**
   * InferType can sometimes generate duplicate hints (e.g. for symbols inside of `for`-comprehensons)
   * That is why we have to deduplicate them when adding new inlay hints
   */
  private def addInlayHint(
      inlayHint: InlayHint,
      origin: InlayHintOrigin
  ): List[InlayHintWithOrigin] = {
    val wrapped = InlayHintWithOrigin(inlayHint, origin)
    // Only filter duplicates for InferredType hints
    if (
      origin == InlayHintOrigin.InferredType && inlayHints.exists(
        _.hint == inlayHint
      )
    ) {
      inlayHints
    } else {
      inlayHints :+ wrapped
    }
  }

  def result(): List[InlayHint] =
    inlayHints.reverse.map(_.hint) ++ blockInlayHints.values.toList
      .flatMap(_.build)
      .map(makeInlayHint)

}

object InlayHints {

  def makeInlayHint(
      pos: l.Range,
      labelParts: List[LabelPart],
      kind: InlayHintKind,
      uri: URI
  ): InlayHint = {
    makeInlayHint(pos.getStart(), labelParts, kind, uri)
  }

  def makeInlayHint(
      pos: l.Position,
      labelParts: List[LabelPart],
      kind: InlayHintKind,
      uri: URI
  ): InlayHint = {
    val hint = new InlayHint()
    hint.setPosition(pos)
    val (label, dataInfo) = labelParts.map(lp => (lp.label, lp.data)).unzip
    hint.setLabel(label.asJava)
    hint.setData(InlayHints.toData(uri.toString(), dataInfo))
    hint.setKind(kind)
    hint
  }
  def empty(uri: URI): InlayHints =
    InlayHints(uri, Nil, Map.empty[Int, InlayHintBlock])

  /**
   * Creates a label for inlay hint by inserting `parts` on correct positions in `tpeStr`.
   *
   * @param parts each contain a single symbol name and its definition position or semanticdb symbol
   * @param tpeStr correct label for the inlay hint
   *
   * Example: for `tpeStr` = `(Int, List[Int])`,
   * `parts` are `List(("Int", "scala/Int#"), ("List", "scala/collection/immutable/List#"), ("Int", "scala/Int#"))`
   */
  def makeLabelParts(
      parts: List[LabelPart],
      tpeStr: String
  ): List[LabelPart] = {
    val buffer = ListBuffer.empty[LabelPart]
    var current = 0
    parts
      .flatMap { lp =>
        tpeStr.allIndexesOf(lp.name).map((_, lp))
        // find all occurences of str in tpe
      }
      .sortWith { case ((idx1, lp1), (idx2, lp2)) =>
        if (idx1 == idx2) lp1.length > lp2.length
        else idx1 < idx2
      }
      .foreach { case (index, lp) =>
        if (index >= current) {
          buffer += LabelPart(tpeStr.substring(current, index))
          buffer += lp
          current = index + lp.length
        }
      }
    buffer += LabelPart(tpeStr.substring(current, tpeStr.length))
    buffer.toList.filter(_.name.nonEmpty)
  }

  /** Hand-rolled JSON encoding (no Gson/reflection -- consistent with this
   *  project's zero-reflection codec philosophy). Returns a plain `String`,
   *  suitable to store directly as the opaque `Object` in
   *  `org.eclipse.lsp4j.InlayHint#data`.
   */
  def toData(uri: String, data: List[Either[String, l.Position]]): String = {
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
    sb.append("{\"uri\":").append(jsonString(uri))
    sb.append(",\"labelParts\":[")
    var first = true
    data.foreach { part =>
      if (!first) sb.append(',')
      part match {
        case Left(str) =>
          sb.append("{\"dataType\":\"string\",\"string\":")
            .append(jsonString(str))
            .append('}')
        case Right(pos) =>
          sb.append("{\"dataType\":\"position\",\"line\":")
            .append(pos.getLine)
            .append(",\"character\":")
            .append(pos.getCharacter)
            .append('}')
      }
      first = false
    }
    sb.append("]}")
    sb.toString
  }

  /** Hand-rolled decode matching `toData`'s exact output shape --
   *  deliberately minimal (this fixed shape only), not a general JSON
   *  parser. Not currently called from any self-hosted code path (only the
   *  JVM test harness called the old Gson-based version), kept for API
   *  completeness.
   */
  def fromData(json: String): (String, List[Either[String, l.Position]]) = {
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

    var uri = ""
    val parts = new java.util.ArrayList[Either[String, l.Position]]()

    expect('{')
    skipWs()
    var continueObj = i < n && json.charAt(i) != '}'
    while (continueObj) {
      val key = parseString()
      expect(':')
      key match {
        case "uri" => uri = parseString()
        case "labelParts" =>
          expect('[')
          skipWs()
          var more = i < n && json.charAt(i) != ']'
          while (more) {
            expect('{')
            var dataType = ""
            var string = ""
            var line = 0
            var character = 0
            skipWs()
            var continueEntry = i < n && json.charAt(i) != '}'
            while (continueEntry) {
              val entryKey = parseString()
              expect(':')
              entryKey match {
                case "dataType" => dataType = parseString()
                case "string" => string = parseString()
                case "line" => line = parseInt()
                case "character" => character = parseInt()
                case _ =>
                  skipWs()
                  if (i < n && json.charAt(i) == '"') parseString() else parseInt()
              }
              skipWs()
              if (i < n && json.charAt(i) == ',') { i += 1; continueEntry = true }
              else continueEntry = false
            }
            expect('}')
            if (dataType == "position") parts.add(Right(new l.Position(line, character)))
            else parts.add(Left(string))
            skipWs()
            if (i < n && json.charAt(i) == ',') { i += 1; more = true }
            else more = false
          }
          expect(']')
        case _ =>
          skipWs()
          if (i < n && json.charAt(i) == '"') parseString() else parseInt()
      }
      skipWs()
      if (i < n && json.charAt(i) == ',') { i += 1; continueObj = true }
      else continueObj = false
    }
    expect('}')

    (uri, parts.asScala.toList)
  }
}

final case class InlayHintBlock(
    indentLevel: Int,
    hints: List[BlockInlayHint]
) {
  def build: List[BlockInlayHint] = {
    if (hints.length == 1) Nil
    else
      hints.map { hint =>
        val naiveIndent = indentLevel - hint.pos.getEnd.getCharacter
        val labels =
          if (naiveIndent <= 0) hint.labels
          else LabelPart(" " * naiveIndent) :: hint.labels
        hint.copy(labels = labels)
      }
  }
}

final case class BlockInlayHint(
    pos: l.Range,
    labels: List[LabelPart],
    kind: InlayHintKind
)

