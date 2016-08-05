package dtos

import java.util.UUID
import java.util.Date
import org.apache.commons.lang3.StringUtils

class WordIndicesDTO(
    var resourceLocationId: UUID,
    var word: String,
    var content: String,
    var contentId: UUID,
    var contentKey1: String,
    var contentKey2: String,
    var contentKey3: String,
    var indices: Map[Int, Int],
    var indicesInResource: Double,
    var nextContent: String,
    var prevContent: String,
    var resourceDisplayLocation: String,
    var resourceIndexerName: String,
    var resourceLastModified: Date,
    var resourceName: String,
    var resourceSize: String,
    var resourceUri: String,
    var resourceWalkerName: String) {
}

object WordIndicesDTO {
  def apply: WordIndicesDTO =
    new WordIndicesDTO(
      UUID.randomUUID(),
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      UUID.randomUUID(),
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      Map.empty,
      0,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      new Date,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY,
      StringUtils.EMPTY)

}
