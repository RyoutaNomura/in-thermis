package jp.co.rn.inthermis.settings

object ApplicationConfig {
  val host: String = "localhost"
  val port: Int = 9042
  val keyspace: String = "mykeyspace"
  var resourceWalkerConfigs: ArrayBuffer[ResourceWalkerConfig] = ArrayBuffer.empty

  resourceWalkerConfigs += ResourceWalkerConfig(
    UUID.randomUUID,
    "Wikipedia",
    URI.create("https://ja.wikipedia.org/w/api.php"),
    "jp.co.rn.inthermis.logic.walker.impl.MediaWikiWalker",
    Map.empty)
  
  val elasticSearchHost: String = "localhost"
  val elasticSearchPort: Int = 9200
  val elasticSearchIndexName: String = "inthermis"
  val elasticSearchType: String = "word_index"
  val elasticSearchUri = s"http://$elasticSearchHost:$elasticSearchPort/"

}

