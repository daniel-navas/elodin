package com.github.caeus.plutus.json

import com.github.caeus.plutus.PackerSyntax.StringPackerSyntax
import com.github.caeus.plutus.PrettyPacker.PackerException
import com.github.caeus.plutus.json.JSON._
import com.github.caeus.plutus.{Packer, PrettyPacker}
import com.jsoniter.Jsoniter
sealed trait JSON

object JSON {

  case object JSNull extends JSON

  case class JSBool(value: Boolean) extends JSON

  case class JSNumber(value: BigDecimal) extends JSON

  case class JSText(value: String) extends JSON

  case class JSArray(value: Seq[JSON]) extends JSON

  case class JSObject(value: Map[String, JSON]) extends JSON

}

class JsonPacker {

  import StringPackerSyntax._

  lazy val nullPacker    = P("null").as(JSNull)
  lazy val booleanPacker = (P("false") | P("true")).!.map(window => JSBool(window.value.toBoolean))
  lazy val digits        = P(_.isDigit).rep(min = 1)
  lazy val exponent      = P("e|E".r) ~ P("""[+\-]""".r).? ~ digits
  lazy val fractional    = P(".") ~ digits
  lazy val integral      = (P("0") | P("""[1-9]""".r)) ~ digits.?
  lazy val numberPacker =
    (P("""[+\-]""".r).? ~ integral ~ fractional.? ~ exponent.?).!.map { window =>
      JSNumber(BigDecimal(window.value))
    }
  lazy val hexDigit      = P("[0-9a-fA-F]".r)
  lazy val unicodeEscape = P("u") ~ hexDigit.rep(min = 4, max = Some(4))
  lazy val escape        = P("\\") ~ (P("[\"/\\\\bfnrt]".r) | unicodeEscape)
  lazy val strChars      = P("""[^\\\"]*""".r)
  lazy val stringPacker = (P("\"") ~ (strChars | escape).rep ~ P("\"")).!.map { window =>
    JSText(Jsoniter.parse(window.value).readString())
  }
  lazy val space: Packer[String, Char, Unit] = P(_.isWhitespace).rep.ignore
  lazy val arrayPacker = (P("[") ~ (space ~ jsonPacker).rep(sep = space ~ P(",")) ~ space ~ P("]"))
    .map(JSArray.apply)
  lazy val objectPacker =
    (P("{") ~ (space ~ stringPacker.map(_.value) ~ space ~ P(":") ~ space ~ jsonPacker)
      .rep(sep = space ~ P(",")) ~ space ~ P("}")).map { tuples =>
      JSObject(tuples.toMap)
    }
  lazy val jsonPacker: Packer[String, Char, JSON] =
    (nullPacker | booleanPacker | numberPacker | stringPacker | arrayPacker | objectPacker)
      .logging("jsonPacker")
  lazy val finalPacker                                = space ~ jsonPacker ~ space ~ end
  lazy val prettyPacker                               = PrettyPacker.version1(finalPacker)
  def run(src: String): Either[PackerException, JSON] = prettyPacker.process(src)
}
