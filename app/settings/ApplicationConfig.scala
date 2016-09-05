package settings

import scala.collection.mutable
import logic.walker.ResourceWalkerConfig
import logic.walker.ResourceWalkerConfig
import java.nio.file.Paths
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import logic.walker.ResourceWalkerConfig
import java.net.URI

object ApplicationConfig {
  val host: String = "localhost"
  val port: Int = 9042
  val keyspace: String = "mykeyspace"
  var resourceWalkerConfigs: ArrayBuffer[ResourceWalkerConfig] = ArrayBuffer.empty

  resourceWalkerConfigs += ResourceWalkerConfig(UUID.randomUUID, "Yugawara(CIFS)", Paths.get("/Users/RyoutaNomura/Desktop/odssample のコピー").toUri, "logic.walker.impl.FileWalker", Map.empty)
  resourceWalkerConfigs += ResourceWalkerConfig(UUID.randomUUID, "Wikipedia", URI.create("https://ja.wikipedia.org/w/api.php"), "logic.walker.impl.MediaWikiWalker", Map.empty)
}

