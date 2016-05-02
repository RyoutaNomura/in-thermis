package logic.walker

import java.net.URI

case class ResourceWalkerConfig(
  val name: String,
  val uri: URI,
  val clazz: Class[ResourceWalker],
  val props: Map[String, String])
