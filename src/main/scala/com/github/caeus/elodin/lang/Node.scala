package com.github.caeus.elodin.lang

case class Meta[+A](value: A, children: List[Meta[A]])

sealed trait Step
object Step {
  case object Down extends Step {
    override def toString: String = "_"
  }
  case class Index(value: Int) extends Step {
    override def toString: String = value.toString
  }
  case class Key(value: String) extends Step {
    override def toString: String = value
  }
}

case class Path(value: Seq[Step]) {
  final def key(value: String): Path = /(Step.Key(value))
  final def index(value: Int): Path  = /(Step.Index(value))
  final def down: Path               = /(Step.Down)
  final def /(step: Step): Path      = Path(value.appended(step))
  final def parent                   = Path(value.init)
  final def head: Step               = value.head
  final def tail: Path               = Path(value.tail)
  override def toString: String      = "\\" + value.mkString(".")

}
object Path {
  def root = Path(Vector.empty)

}
sealed trait Node

object Node {

  case class LetNode(bindings: Map[String, Node], body: Node) extends Node
  case class FnNode(params: Seq[String], body: Node)          extends Node
  case class ApplyNode(args: Seq[Node])                       extends Node
  case class TextNode(value: String)                          extends Node
  case class IntNode(value: BigInt)                           extends Node
  case class FloatNode(value: BigDecimal)                     extends Node
  case class BoolNode(value: Boolean)                         extends Node
  //case class ArrNode(items: Seq[Node])                        extends Node
  //case class DictNode(items: Map[String, Node])               extends Node
  case class RefNode(to: String) extends Node
  case class ReqNode(to: String) extends Node

}
