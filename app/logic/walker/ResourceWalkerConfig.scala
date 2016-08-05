package logic.walker

import java.net.URI
import java.util.UUID

case class ResourceWalkerConfig(
  val id: UUID,
  val name: String,
  val uri: URI,
  val implClassName: String,
  val props: Map[String, String])
