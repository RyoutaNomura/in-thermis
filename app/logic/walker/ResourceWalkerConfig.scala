package logic.walker

import java.net.URI

case class ResourceWalkerConfig(
  val name: String,
  val uri: URI,
  val implClassName: String,
  val props: Map[String, String])
